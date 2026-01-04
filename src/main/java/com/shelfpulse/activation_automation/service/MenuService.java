package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.config.ApplicationProperties;
import com.shelfpulse.activation_automation.dto.menu.MenuDto;
import com.shelfpulse.activation_automation.dto.websocket.MenuProcessingStatusDto;
import com.shelfpulse.activation_automation.entity.Admin;
import com.shelfpulse.activation_automation.entity.Eatery;
import com.shelfpulse.activation_automation.entity.Menu;
import com.shelfpulse.activation_automation.enums.MenuStatus;
import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.repository.AdminRepository;
import com.shelfpulse.activation_automation.repository.EateryRepository;
import com.shelfpulse.activation_automation.repository.MenuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuService.class);

    private final MenuRepository menuRepository;
    private final EateryRepository eateryRepository;
    private final AdminRepository adminRepository;
    private final GcsService gcsService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final ApplicationProperties applicationProperties;

    public MenuService(MenuRepository menuRepository,
            EateryRepository eateryRepository,
            AdminRepository adminRepository,
            GcsService gcsService,
            WebSocketNotificationService webSocketNotificationService,
            ApplicationProperties applicationProperties) {
        this.menuRepository = menuRepository;
        this.eateryRepository = eateryRepository;
        this.adminRepository = adminRepository;
        this.gcsService = gcsService;
        this.webSocketNotificationService = webSocketNotificationService;
        this.applicationProperties = applicationProperties;
    }

    @Transactional
    public Menu uploadMenuImages(Integer eateryId, Integer adminId, UserType userType, List<MultipartFile> menuImages)
            throws Exception {
        Optional<Eatery> eateryOpt = eateryRepository.findById(eateryId);
        if (eateryOpt.isEmpty()) {
            throw new Exception("No eatery found with the provided eateryId.");
        }
        Eatery eatery = eateryOpt.get();

        if (userType != UserType.SUPER_ADMIN && !eatery.getAdmin().getId().equals(adminId)) {
            throw new Exception("No eatery found with the provided eateryId.");
        }

        Optional<Menu> latestMenuOpt = menuRepository.findFirstByEateryIdOrderByCreatedAtDesc(eateryId);
        if (latestMenuOpt.isPresent() && latestMenuOpt.get().getStatus() == MenuStatus.PROCESSING) {
            throw new Exception(
                    "The menu image is still processing. Please wait until it is complete before uploading a new one.");
        }

        if (menuImages == null || menuImages.isEmpty()) {
            throw new Exception("No files uploaded");
        }

        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            throw new Exception("Admin not found.");
        }

        List<String> imageUrls = uploadImagesAndGetUrls(menuImages, adminId, eateryId);
        if (imageUrls.isEmpty()) {
            throw new Exception("Image upload failed, no URLs returned.");
        }

        Menu newMenu = createMenuImageEntry(eatery, adminOpt.get(), imageUrls);

        try {
            triggerAiMenuProcessing(adminId, eateryId);
        } catch (Exception e) {
            log.error("AI processing trigger failed for eateryId {}: {}", eateryId, e.getMessage());
            newMenu.setStatus(MenuStatus.FAILED);
            menuRepository.save(newMenu);
            throw new Exception("Failed to initiate AI processing: " + e.getMessage());
        }

        webSocketNotificationService.notifyMenuProcessingStatus(
                Long.valueOf(adminId),
                Long.valueOf(eateryId),
                MenuProcessingStatusDto.builder()
                        .eateryId(Long.valueOf(eateryId))
                        .status(MenuStatus.PROCESSING)
                        .message("Upload successful. AI processing has now started.")
                        .build());

        return newMenu;
    }

    private List<String> uploadImagesAndGetUrls(List<MultipartFile> files, Integer adminId, Integer eateryId)
            throws Exception {
        List<String> imageUrls = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty())
                continue;

            String originalFilename = file.getOriginalFilename();
            String ext = getExtension(originalFilename);
            String path = String.format("%d/menuImages", eateryId);

            try {
                String url = gcsService.uploadAdminRecognizedImage(
                        Long.valueOf(adminId),
                        file.getBytes(),
                        path,
                        originalFilename,
                        ext);
                imageUrls.add(url);
            } catch (Exception e) {
                log.error("Failed to upload menu image {}: {}", originalFilename, e.getMessage());
                throw new Exception("Failed to upload menu image: " + e.getMessage());
            }
        }

        return imageUrls;
    }

    @Transactional
    private Menu createMenuImageEntry(Eatery eatery, Admin admin, List<String> rawMenuImgUrls) {
        long menuCount = menuRepository.countByEateryId(eatery.getId());
        String templateName = "template_" + (menuCount + 1);

        Menu menu = new Menu();
        menu.setEatery(eatery);
        menu.setAdmin(admin);
        menu.setRawMenuImgUrls(rawMenuImgUrls);
        menu.setTemplateName(templateName);
        menu.setStatus(MenuStatus.PROCESSING);

        Menu savedMenu = menuRepository.save(menu);

        eatery.setStatus(MenuStatus.PROCESSING);
        eateryRepository.save(eatery);

        return savedMenu;
    }

    private void triggerAiMenuProcessing(Integer adminId, Integer eateryId) throws Exception {
        String aiBackendUrl = applicationProperties.getAiBackendUrl();
        String bucketFolderLink = String.format("gs://shelfex-cdn/automation-activation/%d/%d/menuImages", adminId,
                eateryId);

        log.info("Triggering AI menu processing for eateryId: {} with bucket: {}", eateryId, bucketFolderLink);
    }

    @Transactional
    public void processMenu(List<String> jsonUrls) {
        if (jsonUrls == null || jsonUrls.isEmpty()) {
            log.error("processMenu called with no JSON URLs. Aborting.");
            return;
        }

        log.info("AI provided json urls: {}", jsonUrls);

        try {
            int[] ids = extractIdsFromUrl(jsonUrls.get(0));
            int adminId = ids[0];
            int eateryId = ids[1];

            Optional<Menu> menuOpt = menuRepository.findFirstByEateryIdOrderByCreatedAtDesc(eateryId);
            if (menuOpt.isEmpty() || menuOpt.get().getStatus() == MenuStatus.FAILED) {
                log.error("Aborting. Menu record not found or status is 'failed' for eateryId: {}", eateryId);
                return;
            }

            Menu menu = menuOpt.get();
            menu.setStatus(MenuStatus.GENERATED);
            menuRepository.save(menu);

            Optional<Eatery> eateryOpt = eateryRepository.findById(eateryId);
            if (eateryOpt.isPresent()) {
                Eatery eatery = eateryOpt.get();
                eatery.setStatus(MenuStatus.GENERATED);
                eatery.setMenusCreated(eatery.getMenusCreated() + 1);
                eateryRepository.save(eatery);
            }

            webSocketNotificationService.notifyMenuProcessingStatus(
                    Long.valueOf(adminId),
                    Long.valueOf(eateryId),
                    MenuProcessingStatusDto.builder()
                            .eateryId(Long.valueOf(eateryId))
                            .status(MenuStatus.COMPLETED)
                            .message("Menu for eatery " + eateryId + " has been successfully processed.")
                            .build());

            log.info("Menu processing completed successfully for eateryId: {}", eateryId);

        } catch (Exception e) {
            log.error("Menu processing failed: {}", e.getMessage());
        }
    }

    private int[] extractIdsFromUrl(String url) throws Exception {
        String[] urlParts = url.split("/");
        int activationIndex = -1;
        for (int i = 0; i < urlParts.length; i++) {
            if ("automation-activation".equals(urlParts[i])) {
                activationIndex = i;
                break;
            }
        }

        if (activationIndex == -1 || urlParts.length < activationIndex + 3) {
            throw new Exception(
                    "Invalid URL format: Could not find '.../automation-activation/adminId/eateryId/...' in " + url);
        }

        try {
            int adminId = Integer.parseInt(urlParts[activationIndex + 1]);
            int eateryId = Integer.parseInt(urlParts[activationIndex + 2]);
            return new int[] { adminId, eateryId };
        } catch (NumberFormatException e) {
            throw new Exception("Could not parse valid numeric adminId and eateryId from the URL: " + url);
        }
    }

    public MenuDto.MenuStatusResponse getMenuStatus(Integer eateryId, Integer adminId) throws Exception {
        Optional<Menu> menuOpt = menuRepository.findFirstByEateryIdAndAdminIdOrderByCreatedAtDesc(eateryId, adminId);

        if (menuOpt.isEmpty()) {
            return new MenuDto.MenuStatusResponse(eateryId, null, "No menu processing record found.");
        }

        Menu menu = menuOpt.get();
        MenuDto.MenuStatusResponse response = new MenuDto.MenuStatusResponse();
        response.setEateryId(menu.getEatery().getId());
        response.setStatus(menu.getStatus());
        response.setTemplateId(menu.getId());

        if (menu.getStatus() == MenuStatus.COMPLETED) {
            response.setMessage("Menu successfully processed.");
        } else {
            response.setMessage("Menu status: " + menu.getStatus().getValue());
        }

        String dataJsonUrl = menu.getDataJsonUrl();
        if (dataJsonUrl == null && menu.getWorkingDataJsonUrls() != null && !menu.getWorkingDataJsonUrls().isEmpty()) {
            dataJsonUrl = menu.getWorkingDataJsonUrls().get(menu.getWorkingDataJsonUrls().size() - 1);
        }
        response.setDataJsonUrl(dataJsonUrl);

        return response;
    }

    private String getExtension(String filename) {
        if (filename == null)
            return "";
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i) : "";
    }
}
