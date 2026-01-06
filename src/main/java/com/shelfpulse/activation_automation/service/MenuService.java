package com.shelfpulse.activation_automation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfpulse.activation_automation.config.ApplicationProperties;
import com.shelfpulse.activation_automation.dto.menu.MenuDto;
import com.shelfpulse.activation_automation.dto.menu.structure.ParsingDtos;
import com.shelfpulse.activation_automation.dto.menu.structure.StructuredMenuDtos;
import com.shelfpulse.activation_automation.dto.websocket.MenuProcessingStatusDto;
import com.shelfpulse.activation_automation.entity.Admin;
import com.shelfpulse.activation_automation.entity.ComboImage;
import com.shelfpulse.activation_automation.entity.Eatery;
import com.shelfpulse.activation_automation.entity.Menu;
import com.shelfpulse.activation_automation.enums.MenuStatus;
import com.shelfpulse.activation_automation.enums.OrientationType;
import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.repository.AdminRepository;
import com.shelfpulse.activation_automation.repository.EateryRepository;
import com.shelfpulse.activation_automation.repository.MenuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuService.class);

    private final MenuRepository menuRepository;
    private final EateryRepository eateryRepository;
    private final AdminRepository adminRepository;
    private final GcsService gcsService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final ApplicationProperties applicationProperties;
    private final AdminService adminService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MenuService(MenuRepository menuRepository,
            EateryRepository eateryRepository,
            AdminRepository adminRepository,
            GcsService gcsService,
            WebSocketNotificationService webSocketNotificationService,
            ApplicationProperties applicationProperties,
            AdminService adminService,
            ObjectMapper objectMapper) {
        this.menuRepository = menuRepository;
        this.eateryRepository = eateryRepository;
        this.adminRepository = adminRepository;
        this.gcsService = gcsService;
        this.webSocketNotificationService = webSocketNotificationService;
        this.applicationProperties = applicationProperties;
        this.adminService = adminService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Transactional
    public Menu uploadMenuImages(Integer eateryId, Integer adminId, UserType userType, List<MultipartFile> menuImages,
            List<MenuDto.MenuImageInfo> menuImageInfos)
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

        List<String> imageUrls = uploadImagesAndGetUrls(menuImages, menuImageInfos, adminId, eateryId);
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

    private List<String> uploadImagesAndGetUrls(List<MultipartFile> files, List<MenuDto.MenuImageInfo> infos,
            Integer adminId, Integer eateryId)
            throws Exception {
        List<String> imageUrls = new ArrayList<>();
        boolean hasValidInfo = infos != null && infos.size() == files.size();

        if (hasValidInfo) {
            log.info("✔️ Menu image info provided. Applying custom filenames.");
        } else {
            log.info("⚠️ Menu image info not provided. Uploading with default filenames.");
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty())
                continue;

            String originalFilename = file.getOriginalFilename();
            String ext = getExtension(originalFilename);
            String path = String.format("%d/menuImages", eateryId);
            String fileNameToUse = originalFilename;

            if (hasValidInfo) {
                MenuDto.MenuImageInfo info = infos.get(i);
                if (info.getId() != null && info.getSide() != null) {
                    fileNameToUse = String.format("%s_%s%s", info.getId(), info.getSide(), ext);
                }
            }

            try {
                String url = gcsService.uploadAdminRecognizedImage(
                        Long.valueOf(adminId),
                        file.getBytes(),
                        path,
                        fileNameToUse,
                        ext);
                imageUrls.add(url);
            } catch (Exception e) {
                log.error("Failed to upload menu image {}: {}", fileNameToUse, e.getMessage());
                throw new Exception("Failed to upload menu image: " + e.getMessage());
            }
        }

        return imageUrls;
    }

    @Transactional
    public Menu createMenuImageEntry(Eatery eatery, Admin admin, List<String> rawMenuImgUrls) {
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
        String bucketFolderLink = String.format("gs://shelfex-cdn/automation-activation/%d/%d/menuImages", adminId,
                eateryId);
        String url = applicationProperties.getAiBackendUrl() + "/process_menu_folder";

        Map<String, String> payload = new HashMap<>();
        payload.put("bucket_folder_link", bucketFolderLink);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> respMap = objectMapper.readValue(response.body(), new TypeReference<>() {
            });
            if ("processing".equals(respMap.get("status"))) {
                return;
            }
            throw new Exception("AI backend status: " + respMap.get("status"));
        } else {
            throw new Exception("AI backend failed with status: " + response.statusCode());
        }
    }

    @SuppressWarnings("unchecked")
    public void processMenu(List<String> jsonUrls) {
        if (jsonUrls == null || jsonUrls.isEmpty()) {
            log.error("processMenu called with no JSON URLs. Aborting.");
            return;
        }

        log.info("AI provided json urls: {}", jsonUrls);

        int[] ids;
        try {
            ids = extractIdsFromUrl(jsonUrls.get(0));
        } catch (Exception e) {
            log.error("Failed to extract IDs from URL: {}", e.getMessage());
            return;
        }
        int adminId = ids[0];
        int eateryId = ids[1];

        Integer menuIdForErrorHandling = null;

        try {
            ParsingDtos.MenuJsonData metadata = fetchMetadata(adminId, eateryId);
            if (metadata == null) {
                throw new Exception("Failed to fetch critical processing_metadata.json for eatery " + eateryId);
            }

            log.info("[Eatery: {}] Starting menu processing transaction...", eateryId);

            Map<String, Object> result = runProcessingTransaction(eateryId, adminId, jsonUrls, metadata);
            menuIdForErrorHandling = (Integer) result.get("templateId");

            log.info("[Eatery: {}] Transaction completed successfully.", eateryId);

            if (result.get("flatDataJsonUrl") != null) {
                log.info("Sending notifications for eateryId: {}", eateryId);
                sendNotifications(adminId, eateryId, (String) result.get("flatDataJsonUrl"),
                        (long) menuIdForErrorHandling);
            }

        } catch (Exception error) {
            String specificErrorMessage = error.getMessage();
            log.error("[Eatery: {}] Processing failed: {}", eateryId, specificErrorMessage);

            if (error.getMessage().contains("resulted in empty data") && menuIdForErrorHandling != null) {
                log.error("[MenuId: {}] No data was structured. Marking process as failed.", menuIdForErrorHandling);
                try {
                    updateMenuStatus(menuIdForErrorHandling, MenuStatus.FAILED);
                } catch (Exception e) {
                    log.error("Failed to update menu status to FAILED: {}", e.getMessage());
                }
            }
        }
    }

    @Transactional
    public Map<String, Object> runProcessingTransaction(int eateryId, int adminId, List<String> jsonUrls,
            ParsingDtos.MenuJsonData metadata) throws Exception {
        Optional<Menu> menuOpt = menuRepository.findFirstByEateryIdOrderByCreatedAtDesc(eateryId);
        if (menuOpt.isEmpty() || menuOpt.get().getStatus() == MenuStatus.FAILED) {
            throw new Exception("[MenuId: N/A] Aborting. Menu record not found or status is 'failed'.");
        }
        Menu menuData = menuOpt.get();

        Eatery eatery = eateryRepository.findById(eateryId).orElse(null);
        String eateryName = eatery != null ? eatery.getName() : "";
        List<ComboImage> comboImages = adminService.getAllInfoComboImages();

        Map<String, Object> prepared = fetchAndPrepareMenus(jsonUrls,
                menuData.getRawMenuImgUrls() != null ? menuData.getRawMenuImgUrls() : Collections.emptyList());

        @SuppressWarnings("unchecked")
        List<String> sortedJsonUrls = (List<String>) prepared.get("sortedJsonUrls");
        @SuppressWarnings("unchecked")
        List<ParsingDtos.MenuContent> menusToMerge = (List<ParsingDtos.MenuContent>) prepared.get("menusToMerge");

        OrientationType majorityOrientation = getMajorityOrientation(sortedJsonUrls, metadata);

        StructuredMenuDtos.StructuredMenu flatData = structureMenuPages(sortedJsonUrls, menusToMerge, comboImages,
                majorityOrientation, metadata, menuData.getRawMenuImgUrls(), eateryName);

        boolean hasData = flatData.getPages().values().stream()
                .anyMatch(p -> p.getData() != null && !p.getData().isEmpty());
        if (!hasData) {
            throw new Exception("Menu structuring resulted in empty data; process failed.");
        }

        String flatDataJsonUrl = uploadFinalJson(adminId, eateryId, flatData, menuData.getId());

        menuData.setStatus(MenuStatus.GENERATED);
        List<String> workingUrls = menuData.getWorkingDataJsonUrls();
        if (workingUrls == null)
            workingUrls = new ArrayList<>();
        workingUrls.add(flatDataJsonUrl);

        // Keep only the last 10 URLs, soft-delete the rest from GCS
        if (workingUrls.size() > 10) {
            List<String> urlsToDelete = new ArrayList<>(workingUrls.subList(0, workingUrls.size() - 10));
            workingUrls.subList(0, workingUrls.size() - 10).clear();

            // Async soft delete - move to /deleted/ folder in GCS
            if (!urlsToDelete.isEmpty()) {
                try {
                    gcsService.softDeleteGCSFile(urlsToDelete);
                    log.info("Soft-deleted {} old working JSON URLs", urlsToDelete.size());
                } catch (Exception e) {
                    log.error("Failed to soft-delete old working URLs: {}", e.getMessage());
                }
            }
        }

        menuData.setWorkingDataJsonUrls(workingUrls);

        menuRepository.save(menuData);

        if (eatery != null) {
            eatery.setStatus(MenuStatus.GENERATED);
            eatery.setMenusCreated((eatery.getMenusCreated() == null ? 0 : eatery.getMenusCreated()) + 1);
            eateryRepository.save(eatery);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("adminId", adminId);
        res.put("eateryId", eateryId);
        res.put("flatDataJsonUrl", flatDataJsonUrl);
        res.put("templateId", menuData.getId());
        return res;
    }

    private ParsingDtos.MenuJsonData fetchMetadata(int adminId, int eateryId) {
        try {
            String metadataUrl = String.format("%s/%d/%d/menuImages/processing_metadata.json",
                    applicationProperties.getDefaultGcsUrl(), adminId, eateryId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(metadataUrl + "?ts=" + System.currentTimeMillis())).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), ParsingDtos.MenuJsonData.class);
            } else {
                log.error("HTTP error fetching metadata! Status: {} for URL: {}", response.statusCode(), metadataUrl);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to fetch or parse metadata JSON: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> fetchAndPrepareMenus(List<String> jsonUrls, List<String> imageUrls) throws Exception {
        Map<String, String> jsonMap = new HashMap<>();
        for (String url : jsonUrls) {
            String base = Paths.get(URI.create(url).getPath()).getFileName().toString().replace("_menu.json", "");
            jsonMap.put(base, url);
        }

        List<String> sortedJsonUrls = new ArrayList<>();
        for (String imgUrl : imageUrls) {
            String base = getFileNameWithoutExt(imgUrl);
            if (jsonMap.containsKey(base)) {
                sortedJsonUrls.add(jsonMap.get(base));
            }
        }

        if (sortedJsonUrls.isEmpty()) {
            log.warn("No matching JSON URLs found for image URLs in the database. Processing will be empty.");
            Map<String, Object> res = new HashMap<>();
            res.put("sortedJsonUrls", new ArrayList<>());
            res.put("menusToMerge", new ArrayList<>());
            return res;
        }

        List<ParsingDtos.MenuContent> menusToMerge = new ArrayList<>();
        for (String url : sortedJsonUrls) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ParsingDtos.MenuContent content = objectMapper.readValue(response.body(), ParsingDtos.MenuContent.class);
            menusToMerge.add(content);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("sortedJsonUrls", sortedJsonUrls);
        res.put("menusToMerge", menusToMerge);
        return res;
    }

    private OrientationType getMajorityOrientation(List<String> jsonUrls, ParsingDtos.MenuJsonData metadata) {
        Map<String, ParsingDtos.ProcessedFile> processedFiles = metadata.getProcessedFiles();
        if (processedFiles == null || jsonUrls == null || jsonUrls.isEmpty())
            return null;

        Map<String, ParsingDtos.OrientationInfo> nameToOrientationMap = new HashMap<>();
        for (Map.Entry<String, ParsingDtos.ProcessedFile> entry : processedFiles.entrySet()) {
            String imageName = getFileNameWithoutExt(entry.getKey());
            if (entry.getValue().getOrientationInfo() != null) {
                nameToOrientationMap.put(imageName, entry.getValue().getOrientationInfo());
            }
        }

        Map<String, Integer> orientationCounts = new HashMap<>();
        for (String url : jsonUrls) {
            String jsonName = Paths.get(URI.create(url).getPath()).getFileName().toString().replace("_menu.json", "");
            ParsingDtos.OrientationInfo info = nameToOrientationMap.get(jsonName);
            if (info != null) {
                String orientation = info.getCorrectedOrientation() != null ? info.getCorrectedOrientation()
                        : info.getOriginalOrientation();
                if (orientation != null) {
                    orientationCounts.put(orientation, orientationCounts.getOrDefault(orientation, 0) + 1);
                }
            }
        }

        if (orientationCounts.isEmpty())
            return null;

        String majority = Collections.max(orientationCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
        if ("landscape".equalsIgnoreCase(majority))
            return OrientationType.LANDSCAPE;
        if ("portrait".equalsIgnoreCase(majority))
            return OrientationType.PORTRAIT;
        return null;
    }

    private String uploadFinalJson(int adminId, int eateryId, Object data, int menuImageId) throws Exception {
        String fileName = generateFileName(menuImageId);
        byte[] bytes = objectMapper.writeValueAsBytes(data);
        return gcsService.uploadAdminRecognizedImage((long) adminId, bytes, eateryId + "/templateImages/dataJson",
                fileName + ".json", ".json");
    }

    // --- Structuring Logic ---

    private StructuredMenuDtos.StructuredMenu structureMenuPages(
            List<String> jsonUrls,
            List<ParsingDtos.MenuContent> generatedMenus,
            List<ComboImage> comboImages,
            OrientationType orientation,
            ParsingDtos.MenuJsonData metadata,
            List<String> rawImgUrls,
            String eateryName) {

        Map<String, StructuredMenuDtos.ColumnMetadata> nameToColumnMetadataMap = createColumnMetadataMap(metadata);

        StructuredMenuDtos.RestaurantInfo restaurantInfo = null;

        // Merge restaurant_info from all generated menus
        for (ParsingDtos.MenuContent menu : generatedMenus) {
            StructuredMenuDtos.RestaurantInfo info = menu.getRestaurantInfo();
            if (info == null)
                continue;

            if (restaurantInfo == null) {
                // Deep copy would be ideal, but for now we assume fresh object usage or manual
                // copy if needed
                // Here we simply start with the first one found
                restaurantInfo = new StructuredMenuDtos.RestaurantInfo();
                restaurantInfo.setRestaurantName(info.getRestaurantName());
                restaurantInfo.setAddress(info.getAddress());
                restaurantInfo.setHours(info.getHours());
                restaurantInfo.setContact(info.getContact()); // Shallow copy of contact
                restaurantInfo.setCuisineType(
                        info.getCuisineType() != null ? new ArrayList<>(info.getCuisineType()) : new ArrayList<>());
                restaurantInfo
                        .setAdditionalInfo(info.getAdditionalInfo() != null ? new ArrayList<>(info.getAdditionalInfo())
                                : new ArrayList<>());
            } else {
                if (restaurantInfo.getRestaurantName() == null && info.getRestaurantName() != null)
                    restaurantInfo.setRestaurantName(info.getRestaurantName());
                if (restaurantInfo.getAddress() == null && info.getAddress() != null)
                    restaurantInfo.setAddress(info.getAddress());
                if (restaurantInfo.getHours() == null && info.getHours() != null)
                    restaurantInfo.setHours(info.getHours());

                // Merge Contact
                if (restaurantInfo.getContact() == null)
                    restaurantInfo.setContact(new StructuredMenuDtos.ContactInfo());
                StructuredMenuDtos.ContactInfo currContact = restaurantInfo.getContact();
                StructuredMenuDtos.ContactInfo newContact = info.getContact();

                if (newContact != null) {
                    if (currContact.getEmail() == null && newContact.getEmail() != null)
                        currContact.setEmail(newContact.getEmail());
                    if (currContact.getWebsite() == null && newContact.getWebsite() != null)
                        currContact.setWebsite(newContact.getWebsite());

                    if (newContact.getPhone() != null) {
                        List<String> currentPhones = currContact.getPhone() != null
                                ? new ArrayList<>(currContact.getPhone())
                                : new ArrayList<>();
                        Set<String> uniquePhones = new LinkedHashSet<>(currentPhones);
                        uniquePhones.addAll(newContact.getPhone());
                        currContact.setPhone(new ArrayList<>(uniquePhones));
                    }
                }

                // Merge Arrays
                if (info.getCuisineType() != null) {
                    List<String> currentCuisine = restaurantInfo.getCuisineType() != null
                            ? restaurantInfo.getCuisineType()
                            : new ArrayList<>();
                    Set<String> unique = new LinkedHashSet<>(currentCuisine);
                    unique.addAll(info.getCuisineType());
                    restaurantInfo.setCuisineType(new ArrayList<>(unique));
                }

                if (info.getAdditionalInfo() != null) {
                    List<String> currentAdd = restaurantInfo.getAdditionalInfo() != null
                            ? restaurantInfo.getAdditionalInfo()
                            : new ArrayList<>();
                    Set<String> unique = new LinkedHashSet<>(currentAdd);
                    unique.addAll(info.getAdditionalInfo());
                    restaurantInfo.setAdditionalInfo(new ArrayList<>(unique));
                }
            }
        }

        if (restaurantInfo == null && eateryName != null && !eateryName.isEmpty()) {
            restaurantInfo = new StructuredMenuDtos.RestaurantInfo();
            restaurantInfo.setRestaurantName(eateryName);
        } else if (restaurantInfo != null
                && (restaurantInfo.getRestaurantName() == null || restaurantInfo.getRestaurantName().isEmpty())
                && eateryName != null) {
            restaurantInfo.setRestaurantName(eateryName);
        }

        Map<String, StructuredMenuDtos.MenuPage> pagedResult = new LinkedHashMap<>();
        Set<String> usedImageUrls = new HashSet<>();
        int idCounter = 1;

        // Filter valid menus first for consecutive page keys
        List<ParsingDtos.MenuContent> validMenus = new ArrayList<>();
        List<String> validUrls = new ArrayList<>();

        for (int i = 0; i < generatedMenus.size(); i++) {
            ParsingDtos.MenuContent c = generatedMenus.get(i);
            if (c.getSections() != null && !c.getSections().isEmpty()) {
                validMenus.add(c);
                validUrls.add(jsonUrls.get(i));
            }
        }

        if (validMenus.isEmpty()) {
            log.warn("No valid menus with sections found. Returning empty pages object.");
            StructuredMenuDtos.StructuredMenu result = new StructuredMenuDtos.StructuredMenu();
            result.setPages(new LinkedHashMap<>());
            StructuredMenuDtos.MetaData meta = new StructuredMenuDtos.MetaData();
            meta.setRestaurantInfo(restaurantInfo);
            meta.setOrientation(orientation != null ? orientation.getValue() : null);
            meta.setRawImgUrls(rawImgUrls);
            meta.setSize("A4");
            result.setMetaData(meta);
            return result;
        }

        for (int i = 0; i < validMenus.size(); i++) {
            ParsingDtos.MenuContent menuContent = validMenus.get(i);
            String originalUrl = validUrls.get(i);

            String jsonName = Paths.get(URI.create(originalUrl).getPath()).getFileName().toString()
                    .replace("_menu.json", "");
            StructuredMenuDtos.ColumnMetadata columnData = nameToColumnMetadataMap.get(jsonName);

            List<ParsingDtos.MenuSection> sections = processMenuSections(menuContent.getSections());

            int totalItems = sections.stream().mapToInt(s -> s.getItems() == null ? 0 : s.getItems().size()).sum();
            if (columnData != null) {
                columnData.setTotalItems(totalItems);
                if (totalItems > 45 && (columnData.getColumnsCount() == 1 || columnData.getColumnsCount() == 2)) {
                    columnData.setColumnsCount(3);
                } else if (columnData.getColumnsCount() == 1) {
                    columnData.setColumnsCount(2);
                }
            }

            List<StructuredMenuDtos.ComboImageDto> recommendedImages = mapImagesToSections(sections, comboImages,
                    usedImageUrls);

            Map.Entry<List<StructuredMenuDtos.FlattenedNode>, Integer> flattened = flattenPageData(sections,
                    recommendedImages, idCounter);
            List<StructuredMenuDtos.FlattenedNode> flatData = flattened.getKey();
            idCounter = flattened.getValue();

            // LAST PAGE additions
            if (i == validMenus.size() - 1) {
                if (restaurantInfo != null) {
                    StructuredMenuDtos.FlattenedNode infoNode = new StructuredMenuDtos.FlattenedNode();
                    infoNode.setId("info-" + (idCounter++));
                    infoNode.setType("restaurant_info");
                    infoNode.setValue(restaurantInfo);
                    flatData.add(infoNode);
                }

                String qrUrl = "https://storage.googleapis.com/shelfex-cdn/automation-activation/17/51/menuImages/7bf616.png";
                if (qrUrl != null) {
                    StructuredMenuDtos.FlattenedNode qrNode = new StructuredMenuDtos.FlattenedNode();
                    qrNode.setId("qr-" + (idCounter++));
                    qrNode.setType("qr_code");
                    qrNode.setImgUrl(qrUrl);
                    flatData.add(qrNode);
                }
            }

            StructuredMenuDtos.MenuPage page = new StructuredMenuDtos.MenuPage();
            page.setData(flatData);
            page.setColumnData(columnData);
            page.setRecommendedComboImg(recommendedImages);

            pagedResult.put("page_" + (i + 1), page);
        }

        StructuredMenuDtos.StructuredMenu result = new StructuredMenuDtos.StructuredMenu();
        result.setPages(pagedResult);
        StructuredMenuDtos.MetaData meta = new StructuredMenuDtos.MetaData();
        meta.setRestaurantInfo(restaurantInfo);
        meta.setOrientation(orientation != null ? orientation.getValue() : null);
        meta.setRawImgUrls(rawImgUrls);
        meta.setSize("A4");
        result.setMetaData(meta);

        return result;
    }

    private List<ParsingDtos.MenuSection> processMenuSections(List<ParsingDtos.MenuSection> rawSections) {
        Map<String, ParsingDtos.MenuSection> map = new LinkedHashMap<>();
        for (ParsingDtos.MenuSection s : rawSections) {
            if (s.getTitle() == null)
                continue;
            String key = s.getTitle().trim().toLowerCase();
            if (!map.containsKey(key)) {
                ParsingDtos.MenuSection newS = new ParsingDtos.MenuSection();
                newS.setTitle(s.getTitle().trim());
                newS.setNote(s.getNote());
                newS.setItems(new ArrayList<>());
                map.put(key, newS);
            }
            ParsingDtos.MenuSection merged = map.get(key);
            if (s.getItems() != null) {
                for (ParsingDtos.MenuItem item : s.getItems()) {
                    normalizeItem(item);
                    merged.getItems().add(item);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private void normalizeItem(ParsingDtos.MenuItem item) {
        String vegRegex = "^(pure[\\s._-]*)?veg(etarian)?\\.?$";
        String nonVegRegex = "^non[\\s._-]*veg(etarian)?\\.?$";
        String halfRegex = "^(half|1\\/2|hlf)$";
        String fullRegex = "^(full|ful|fl)$";

        List<String> currentDietInfo = item.getDietaryInfoList();
        if (currentDietInfo == null)
            currentDietInfo = new ArrayList<>();

        String primaryDietType = null;

        Pattern pVeg = Pattern.compile(vegRegex, Pattern.CASE_INSENSITIVE);
        Pattern pNonVeg = Pattern.compile(nonVegRegex, Pattern.CASE_INSENSITIVE);
        Pattern pHalf = Pattern.compile(halfRegex, Pattern.CASE_INSENSITIVE);
        Pattern pFull = Pattern.compile(fullRegex, Pattern.CASE_INSENSITIVE);

        for (String tag : currentDietInfo) {
            String cleanT = tag != null ? tag.trim() : "";
            if (cleanT.isEmpty())
                continue;
            if (pVeg.matcher(cleanT).matches()) {
                primaryDietType = "veg";
            } else if (pNonVeg.matcher(cleanT).matches()) {
                primaryDietType = "non-veg";
            }
        }

        if (item.getPrices() != null) {
            for (ParsingDtos.ItemPriceRow price : item.getPrices()) {
                if (price.getDietType() != null) {
                    String t = price.getDietType().trim();
                    if (pVeg.matcher(t).matches())
                        price.setDietType("veg");
                    else if (pNonVeg.matcher(t).matches())
                        price.setDietType("non-veg");
                }

                if (price.getPortion() != null) {
                    String p = price.getPortion().trim();
                    if (pHalf.matcher(p).matches())
                        price.setPortion("half");
                    else if (pFull.matcher(p).matches())
                        price.setPortion("full");
                }

                if (primaryDietType != null && price.getDietType() == null) {
                    price.setDietType(primaryDietType);
                }
            }
        }

        List<String> cleanTags = new ArrayList<>();
        for (String tag : currentDietInfo) {
            String cleanT = tag != null ? tag.trim() : "";
            if (cleanT.isEmpty())
                continue;
            if (!pVeg.matcher(cleanT).matches() && !pNonVeg.matcher(cleanT).matches()) {
                cleanTags.add(cleanT);
            }
        }
        item.setDietaryInfo(cleanTags.isEmpty() ? null : cleanTags);
    }

    private Map.Entry<List<StructuredMenuDtos.FlattenedNode>, Integer> flattenPageData(
            List<ParsingDtos.MenuSection> sections,
            List<StructuredMenuDtos.ComboImageDto> recommendedImages,
            int idCounter) {

        Set<String> processedTitles = new HashSet<>();
        ParsingDtos.MenuSection headerSection = null;
        StructuredMenuDtos.ComboImageDto headerImage = null;
        ParsingDtos.MenuSection footerSection = null;
        StructuredMenuDtos.ComboImageDto footerImage = null;

        // [0] is the HEADER
        if (!recommendedImages.isEmpty()) {
            headerImage = recommendedImages.get(0);
            String ht = headerImage.getSectionTitle();
            headerSection = sections.stream().filter(s -> s.getTitle().equals(ht)).findFirst().orElse(null);
            if (headerSection != null)
                processedTitles.add(headerSection.getTitle());
        }

        // [1] is the FOOTER image
        if (recommendedImages.size() > 1) {
            footerImage = recommendedImages.get(1);
            String ft = footerImage.getSectionTitle();
            ParsingDtos.MenuSection fs = sections.stream().filter(s -> s.getTitle().equals(ft)).findFirst()
                    .orElse(null);
            if (fs != null && !processedTitles.contains(fs.getTitle())) {
                footerSection = fs;
                processedTitles.add(footerSection.getTitle());
            } else {
                footerSection = null;
                footerImage = null;
            }
        }

        int[] counterPtr = { idCounter };

        List<StructuredMenuDtos.FlattenedNode> headerItems = headerSection != null
                ? createFlatSection(headerSection, headerImage, "before", counterPtr)
                : new ArrayList<>();

        List<StructuredMenuDtos.FlattenedNode> footerItems = footerSection != null
                ? createFlatSection(footerSection, footerImage, "after", counterPtr)
                : new ArrayList<>();

        // Get ALL other sections
        List<StructuredMenuDtos.FlattenedNode> middleItems = new ArrayList<>();
        for (ParsingDtos.MenuSection s : sections) {
            if (!processedTitles.contains(s.getTitle())) {
                middleItems.addAll(createFlatSection(s, null, "none", counterPtr));
            }
        }

        List<StructuredMenuDtos.FlattenedNode> result = new ArrayList<>();
        result.addAll(headerItems);
        result.addAll(middleItems);
        result.addAll(footerItems);

        return new AbstractMap.SimpleEntry<>(result, counterPtr[0]);
    }

    private List<StructuredMenuDtos.FlattenedNode> createFlatSection(
            ParsingDtos.MenuSection section,
            StructuredMenuDtos.ComboImageDto image,
            String position,
            int[] idCounter) {

        List<StructuredMenuDtos.FlattenedNode> results = new ArrayList<>();
        if (image != null && "before".equals(position)) {
            StructuredMenuDtos.FlattenedNode img = new StructuredMenuDtos.FlattenedNode();
            img.setId("image-" + (idCounter[0]++));
            img.setType("image1");
            img.setImgUrl(image.getCompressImgUrl());
            results.add(img);
        }

        StructuredMenuDtos.FlattenedNode sec = new StructuredMenuDtos.FlattenedNode();
        sec.setId("section-" + (idCounter[0]++));
        sec.setType("section");
        sec.setTitle(section.getTitle());
        results.add(sec);

        if (section.getItems() != null) {
            for (ParsingDtos.MenuItem item : section.getItems()) {
                StructuredMenuDtos.FlattenedNode itemNode = new StructuredMenuDtos.FlattenedNode();
                itemNode.setId("item-" + (idCounter[0]++));
                itemNode.setType("item");
                itemNode.setName(item.getName());
                itemNode.setText(item.getDescription());
                if (item.getPrices() != null) {
                    itemNode.setPrices(item.getPrices().stream()
                            .map(p -> new StructuredMenuDtos.ItemPrice(p.getDietType(), p.getPortion(), p.getPrice()))
                            .collect(Collectors.toList()));
                }
                results.add(itemNode);
            }
        }

        if (image != null && "after".equals(position)) {
            StructuredMenuDtos.FlattenedNode img = new StructuredMenuDtos.FlattenedNode();
            img.setId("image-" + (idCounter[0]++));
            img.setType("image1");
            img.setImgUrl(image.getCompressImgUrl());
            results.add(img);
        }

        return results;
    }

    private List<StructuredMenuDtos.ComboImageDto> mapImagesToSections(
            List<ParsingDtos.MenuSection> sections,
            List<ComboImage> comboImages,
            Set<String> usedImageUrls) {

        class ImageMatch {
            ParsingDtos.MenuSection section;
            ComboImage image;
            int score;

            ImageMatch(ParsingDtos.MenuSection s, ComboImage i, int sc) {
                section = s;
                image = i;
                score = sc;
            }
        }

        List<ImageMatch> potentialMatches = new ArrayList<>();

        for (ParsingDtos.MenuSection section : sections) {
            if (section.getTitle() == null)
                continue;
            String searchTitle = section.getTitle().toLowerCase();

            for (ComboImage image : comboImages) {
                if (image.getInfo() == null || usedImageUrls.contains(image.getImgUrl()))
                    continue;

                List<Pattern> keywords = image.getInfo().stream()
                        .filter(s -> s != null && !s.trim().isEmpty())
                        .map(s -> Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE))
                        .collect(Collectors.toList());

                if (keywords.isEmpty())
                    continue;

                int currentScore = 0;
                // A. Section title match
                if (keywords.stream().anyMatch(p -> p.matcher(searchTitle).find()))
                    currentScore++;

                // B. Item names match
                if (section.getItems() != null) {
                    for (ParsingDtos.MenuItem item : section.getItems()) {
                        if (item.getName() != null
                                && keywords.stream().anyMatch(p -> p.matcher(item.getName().toLowerCase()).find())) {
                            currentScore++;
                        }
                    }
                }

                if (currentScore > 0) {
                    potentialMatches.add(new ImageMatch(section, image, currentScore));
                }
            }
        }

        potentialMatches.sort((a, b) -> b.score - a.score);

        List<StructuredMenuDtos.ComboImageDto> results = new ArrayList<>();
        Set<String> assignedSections = new HashSet<>();

        for (ImageMatch match : potentialMatches) {
            if (!assignedSections.contains(match.section.getTitle())
                    && !usedImageUrls.contains(match.image.getImgUrl())) {
                assignedSections.add(match.section.getTitle());
                usedImageUrls.add(match.image.getImgUrl());

                StructuredMenuDtos.ComboImageDto dto = new StructuredMenuDtos.ComboImageDto();
                dto.setImgUrl(match.image.getImgUrl());
                dto.setCompressImgUrl(match.image.getCompressImgUrl());
                dto.setInfo(match.image.getInfo());
                dto.setImgName(match.image.getImgName());
                dto.setIsFavorite(match.image.getIsFavorite());
                dto.setSectionTitle(match.section.getTitle());
                results.add(dto);
            }
        }

        List<StructuredMenuDtos.ComboImageDto> defaultImages = new ArrayList<>();
        // Hardcoded defaults from TS
        StructuredMenuDtos.ComboImageDto d1 = new StructuredMenuDtos.ComboImageDto();
        d1.setImgUrl(
                "https://storage.googleapis.com/shelfex-cdn/automation-activation/3/comboImages/1753081335524_24048..jpg");
        d1.setCompressImgUrl(
                "https://storage.googleapis.com/shelfex-cdn/automation-activation/1/comboImages/compressedImage/1753081335524_24048..webp");
        d1.setInfo(Collections.singletonList("default-image-1"));
        defaultImages.add(d1);

        StructuredMenuDtos.ComboImageDto d2 = new StructuredMenuDtos.ComboImageDto();
        d2.setImgUrl(
                "https://storage.googleapis.com/shelfex-cdn/automation-activation/3/comboImages/1753081508062_BURGE..jpg");
        d2.setCompressImgUrl(
                "https://storage.googleapis.com/shelfex-cdn/automation-activation/1/comboImages/compressedImage/1753081508062_BURGE..webp");
        d2.setInfo(Collections.singletonList("default-image-2"));
        defaultImages.add(d2);

        if (results.size() < 2) {
            Set<String> assignedTitles = results.stream().map(StructuredMenuDtos.ComboImageDto::getSectionTitle)
                    .collect(Collectors.toSet());
            List<ParsingDtos.MenuSection> availableSections = sections.stream()
                    .filter(s -> !assignedTitles.contains(s.getTitle()))
                    .collect(Collectors.toList());

            int defaultsNeeded = 2 - results.size();
            if (defaultsNeeded == 2 && availableSections.size() >= 1) {
                ParsingDtos.MenuSection first = availableSections.size() > 1 ? availableSections.get(1)
                        : availableSections.get(0);
                StructuredMenuDtos.ComboImageDto i1 = copyDto(defaultImages.get(0));
                i1.setSectionTitle(first.getTitle());
                results.add(i1);

                if (availableSections.size() > 1) {
                    ParsingDtos.MenuSection last = availableSections.get(availableSections.size() - 1);
                    StructuredMenuDtos.ComboImageDto i2 = copyDto(defaultImages.get(1));
                    i2.setSectionTitle(last.getTitle());
                    results.add(i2);
                }
            } else if (defaultsNeeded == 1 && availableSections.size() >= 1) {
                ParsingDtos.MenuSection target = availableSections.get(availableSections.size() - 1);
                StructuredMenuDtos.ComboImageDto i1 = copyDto(defaultImages.get(0));
                i1.setSectionTitle(target.getTitle());
                results.add(i1);
            }
        }
        return results;
    }

    private StructuredMenuDtos.ComboImageDto copyDto(StructuredMenuDtos.ComboImageDto src) {
        StructuredMenuDtos.ComboImageDto d = new StructuredMenuDtos.ComboImageDto();
        d.setImgUrl(src.getImgUrl());
        d.setCompressImgUrl(src.getCompressImgUrl());
        d.setInfo(src.getInfo());
        return d;
    }

    private Map<String, StructuredMenuDtos.ColumnMetadata> createColumnMetadataMap(ParsingDtos.MenuJsonData metadata) {
        Map<String, StructuredMenuDtos.ColumnMetadata> map = new HashMap<>();
        if (metadata != null && metadata.getProcessedFiles() != null) {
            for (Map.Entry<String, ParsingDtos.ProcessedFile> e : metadata.getProcessedFiles().entrySet()) {
                String name = getFileNameWithoutExt(e.getKey());
                if (e.getValue().getColumnMetadata() != null) {
                    map.put(name, e.getValue().getColumnMetadata());
                }
            }
        }
        return map;
    }

    private void sendNotifications(int adminId, int eateryId, String dataJsonUrl, long templateId) {
        webSocketNotificationService.notifyMenuProcessingStatus(
                (long) adminId, (long) eateryId,
                MenuProcessingStatusDto.builder()
                        .eateryId((long) eateryId)
                        .status(MenuStatus.COMPLETED)
                        .message("Menu for eatery " + eateryId + " has been successfully processed.")
                        .dataJsonUrl(dataJsonUrl)
                        .templateId(templateId)
                        .build());
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

    private String getFileNameWithoutExt(String path) {
        // Handle full URLs by extracting path component first
        String filePath = path;
        if (path.startsWith("http://") || path.startsWith("https://")) {
            try {
                filePath = URI.create(path).getPath();
            } catch (Exception e) {
                // Fall back to original path if URI parsing fails
            }
        }
        String filename = Paths.get(filePath).getFileName().toString();
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private String getExtension(String filename) {
        if (filename == null)
            return "";
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i) : "";
    }

    private String generateFileName(int menuImageId) {
        return menuImageId + "_" + System.currentTimeMillis() + Double.toString(Math.random()).substring(2, 9);
    }

    private void updateMenuStatus(Integer menuId, MenuStatus status) {
        Optional<Menu> menu = menuRepository.findById(menuId);
        if (menu.isPresent()) {
            menu.get().setStatus(status);
            menuRepository.save(menu.get());
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
}
