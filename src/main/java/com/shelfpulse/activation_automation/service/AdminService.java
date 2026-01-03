package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.dto.admin.AdminDto;
import com.shelfpulse.activation_automation.entity.Admin;
import com.shelfpulse.activation_automation.entity.ComboImage;
import com.shelfpulse.activation_automation.enums.DeletionStatus;
import com.shelfpulse.activation_automation.enums.MenuStatus;
import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.repository.AdminRepository;
import com.shelfpulse.activation_automation.repository.ComboImageRepository;
import com.shelfpulse.activation_automation.repository.EateryRepository;
import com.shelfpulse.activation_automation.repository.MenuRepository;
import com.shelfpulse.activation_automation.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final AdminRepository adminRepository;
    private final ComboImageRepository comboImageRepository;
    private final EateryRepository eateryRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GcsService gcsService;

    public AdminService(AdminRepository adminRepository, ComboImageRepository comboImageRepository,
            EateryRepository eateryRepository, MenuRepository menuRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil, GcsService gcsService) {
        this.adminRepository = adminRepository;
        this.comboImageRepository = comboImageRepository;
        this.eateryRepository = eateryRepository;
        this.menuRepository = menuRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.gcsService = gcsService;
    }

    public Map<String, Object> login(String usernameOrEmail, String password) {
        Admin admin = adminRepository.findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                .orElse(null);

        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            return null;
        }

        String token = jwtUtil.generateToken(admin);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", admin);

        return response;
    }

    public Admin getInfo(Integer adminId) {
        return adminRepository.findById(adminId).orElse(null);
    }

    @Transactional
    public Admin updateAdmin(Integer adminId, AdminDto.UpdateRequest request, MultipartFile profileImage) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (request.getEmail() != null) {
            admin.setEmail(request.getEmail());
        }
        if (request.getUsername() != null) {
            admin.setUsername(request.getUsername());
        }
        if (request.getFirstName() != null) {
            admin.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            admin.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            admin.setPhoneNumber(request.getPhoneNumber());
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String originalFilename = profileImage.getOriginalFilename();
                String fileExtension = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg";
                String uploadedImgUrl = gcsService.uploadAdminRecognizedImage(
                        adminId.longValue(), profileImage.getBytes(), "profilePictures", originalFilename,
                        fileExtension);
                admin.setProfileImg(uploadedImgUrl);
            } catch (Exception e) {
                log.error("Failed to upload profile image: {}", e.getMessage());
                throw new RuntimeException("Failed to upload profile image");
            }
        }

        return adminRepository.save(admin);
    }

    @Transactional
    public List<ComboImage> uploadComboImages(Integer adminId, List<MultipartFile> files,
            String imgName, Boolean isFavorite, List<String> info) {
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("No files uploaded!");
        }

        long totalComboImages = comboImageRepository.countByStatus(DeletionStatus.ACTIVE);
        List<ComboImage> createdImages = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            try {
                byte[] originalBytes = file.getBytes();
                byte[] compressedBytes = compressImage(originalBytes);

                String generatedImgName = imgName != null
                        ? (files.size() == 1 ? imgName : imgName + "_" + (i + 1))
                        : "combo_image_" + (totalComboImages + i + 1);

                String originalUrl = gcsService.uploadAdminRecognizedImage(
                        adminId.longValue(), originalBytes, "comboImages/originalImage",
                        generatedImgName + ".jpg", ".jpg");

                String compressedUrl = gcsService.uploadAdminRecognizedImage(
                        adminId.longValue(), compressedBytes, "comboImages/compressedImage",
                        generatedImgName + ".webp", ".webp");

                ComboImage comboImage = new ComboImage();
                comboImage.setImgName(generatedImgName);
                comboImage.setImgUrl(originalUrl);
                comboImage.setCompressImgUrl(compressedUrl);
                comboImage.setIsFavorite(isFavorite != null && isFavorite);
                comboImage.setInfo(info != null ? info : new ArrayList<>());
                comboImage.setStatus(DeletionStatus.ACTIVE);

                createdImages.add(comboImageRepository.save(comboImage));
            } catch (Exception e) {
                log.error("Failed to upload combo image: {}", e.getMessage());
                throw new RuntimeException("Failed to process combo images: " + e.getMessage());
            }
        }

        return createdImages;
    }

    private byte[] compressImage(byte[] originalBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (image == null) {
                log.warn("Could not read image, returning original bytes");
                return originalBytes;
            }

            BufferedImage rgbImage = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgbImage.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            javax.imageio.ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            javax.imageio.ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(0.7f);

            jpgWriter.setOutput(ImageIO.createImageOutputStream(baos));
            jpgWriter.write(null, new javax.imageio.IIOImage(rgbImage, null, null), jpgWriteParam);
            jpgWriter.dispose();

            return baos.toByteArray();
        } catch (Exception e) {
            log.warn("Image compression failed, returning original bytes: {}", e.getMessage());
            return originalBytes;
        }
    }

    public Page<ComboImage> getPaginatedComboImages(AdminDto.GetComboImagesRequest request) {
        String sortField = "uploadedAt".equals(request.getSortBy()) ? "uploadedAt" : "imgName";
        Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortOrder()) ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by(direction, sortField));

        if (request.getSearch() != null && request.getIsFavorite() != null) {
            return comboImageRepository.findByStatusAndImgNameContainingIgnoreCaseAndIsFavorite(
                    DeletionStatus.ACTIVE, request.getSearch(), request.getIsFavorite(), pageable);
        } else if (request.getSearch() != null) {
            return comboImageRepository.findByStatusAndImgNameContainingIgnoreCase(
                    DeletionStatus.ACTIVE, request.getSearch(), pageable);
        } else if (request.getIsFavorite() != null) {
            return comboImageRepository.findByStatusAndIsFavorite(
                    DeletionStatus.ACTIVE, request.getIsFavorite(), pageable);
        }
        return comboImageRepository.findByStatus(DeletionStatus.ACTIVE, pageable);
    }

    @Transactional
    public void deleteComboImages(List<Integer> comboImageIds) {
        comboImageRepository.updateStatusByIds(comboImageIds, DeletionStatus.INACTIVE);
    }

    public AdminDto.HomeStatsResponse getHomeStats(Integer adminId, UserType userType) {
        boolean isSuperAdmin = userType == UserType.SUPER_ADMIN;

        long totalEateries;
        long completedMenus;
        long generatedMenus;
        long pendingMenus;

        if (isSuperAdmin) {
            totalEateries = eateryRepository.countByStatusNot(MenuStatus.INACTIVE);
            completedMenus = menuRepository.countByStatus(MenuStatus.COMPLETED);
            generatedMenus = menuRepository.countByStatus(MenuStatus.GENERATED);
            pendingMenus = menuRepository.countByStatus(MenuStatus.PROCESSING);
        } else {
            totalEateries = eateryRepository.countByStatusNotAndAdminId(MenuStatus.INACTIVE, adminId);
            completedMenus = menuRepository.countByStatusAndAdminId(MenuStatus.COMPLETED, adminId);
            generatedMenus = menuRepository.countByStatusAndAdminId(MenuStatus.GENERATED, adminId);
            pendingMenus = menuRepository.countByStatusAndAdminId(MenuStatus.PROCESSING, adminId);
        }

        return new AdminDto.HomeStatsResponse(totalEateries, completedMenus, generatedMenus, pendingMenus);
    }

    @Transactional
    public ComboImage updateComboImage(Integer comboImageId, AdminDto.UpdateComboImageRequest request) {
        ComboImage comboImage = comboImageRepository.findById(comboImageId)
                .orElseThrow(() -> new RuntimeException("Combo image not found"));

        if (request.getImgName() != null) {
            comboImage.setImgName(request.getImgName());
        }
        if (request.getIsFavorite() != null) {
            comboImage.setIsFavorite(request.getIsFavorite());
        }
        if (request.getInfo() != null) {
            comboImage.setInfo(request.getInfo());
        }
        if (request.getStatus() != null) {
            comboImage.setStatus(request.getStatus());
        }

        return comboImageRepository.save(comboImage);
    }

    @Transactional
    public Admin createAdmin(AdminDto.CreateAdminRequest request) {
        if (adminRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists.");
        }
        if (adminRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists.");
        }

        Admin admin = new Admin();
        admin.setEmail(request.getEmail());
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setUserType(UserType.ADMIN);

        // Default profile image if not provided (though this route doesn't support file
        // upload)
        admin.setProfileImg(""); // Or some default URL

        return adminRepository.save(admin);
    }

    public long countComboImages() {
        return comboImageRepository.countByStatus(DeletionStatus.ACTIVE);
    }

    public List<ComboImage> getAllInfoComboImages() {
        return comboImageRepository.findAllActiveWithInfo(DeletionStatus.ACTIVE);
    }
}
