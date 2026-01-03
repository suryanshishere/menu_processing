package com.shelfpulse.activation_automation.controller;

import com.shelfpulse.activation_automation.dto.admin.AdminDto;
import com.shelfpulse.activation_automation.entity.Admin;
import com.shelfpulse.activation_automation.entity.ComboImage;
import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    private Integer getAdminIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Integer.parseInt(auth.getName());
    }

    private UserType getUserTypeFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object userTypeObj = auth.getCredentials();
        if (userTypeObj instanceof UserType) {
            return (UserType) userTypeObj;
        }
        return UserType.ADMIN;
    }

    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        try {
            Integer adminId = getAdminIdFromAuth();
            Admin admin = adminService.getInfo(adminId);
            if (admin == null) {
                return ResponseEntity.status(404).body(Map.of("status", false, "message", "Admin not found!"));
            }
            return ResponseEntity.ok(Map.of("status", true, "data", admin));
        } catch (Exception e) {
            log.error("Error fetching admin info: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("status", false, "message",
                    e.getMessage() != null ? e.getMessage() : "Unexpected error occurred while fetching admin info."));
        }
    }

    @GetMapping("/home_stats")
    public ResponseEntity<?> getHomeStats() {
        try {
            Integer adminId = getAdminIdFromAuth();
            UserType userType = getUserTypeFromAuth();
            AdminDto.HomeStatsResponse stats = adminService.getHomeStats(adminId, userType);
            return ResponseEntity.ok(
                    Map.of("status", true, "message", "Dashboard statistics retrieved successfully.", "data", stats));
        } catch (Exception e) {
            log.error("Error fetching home stats: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("status", false, "message", e.getMessage() != null ? e.getMessage()
                            : "Unexpected error occurred while fetching dashboard statistics."));
        }
    }

    private static final java.util.Set<String> ALLOWED_UPDATE_FIELDS = java.util.Set.of(
            "profileImage", "email", "username", "firstName", "lastName", "phoneNumber");

    @PatchMapping(value = "/update_info", consumes = "multipart/form-data")
    public ResponseEntity<?> updateInfo(
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            java.util.Set<String> receivedParams = new java.util.HashSet<>(httpRequest.getParameterMap().keySet());
            if (httpRequest instanceof org.springframework.web.multipart.MultipartHttpServletRequest multipartRequest) {
                receivedParams.addAll(multipartRequest.getFileMap().keySet());
            }

            for (String param : receivedParams) {
                if (!ALLOWED_UPDATE_FIELDS.contains(param)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "status", false,
                            "message", "Invalid fields provided in the request body. Unknown field: '" + param + "'"));
                }
            }

            Integer adminId = getAdminIdFromAuth();

            if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", false, "message", "Please provide a valid email address."));
            }
            if (username != null && username.length() < 3) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", false, "message", "Username must be at least 3 characters long."));
            }
            if (firstName != null && firstName.length() < 1) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", false, "message", "First name cannot be empty."));
            }
            if (lastName != null && lastName.length() < 1) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", false, "message", "Last name cannot be empty."));
            }
            if (phoneNumber != null && !phoneNumber.matches("^\\+?[0-9-]{7,20}$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", false, "message", "Please provide a valid phone number format."));
            }

            AdminDto.UpdateRequest request = new AdminDto.UpdateRequest();
            request.setEmail(email);
            request.setUsername(username);
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setPhoneNumber(phoneNumber);

            boolean hasNoData = email == null && username == null
                    && firstName == null && lastName == null
                    && phoneNumber == null && (profileImage == null || profileImage.isEmpty());

            if (hasNoData) {
                return ResponseEntity.badRequest().body(Map.of("status", false, "message", "No update data provided."));
            }

            Admin updatedAdmin = adminService.updateAdmin(adminId, request, profileImage);
            if (updatedAdmin == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("status", false, "message", "Admin not found or no changes were applied."));
            }

            return ResponseEntity
                    .ok(Map.of("status", true, "message", "Admin profile updated successfully.", "data", updatedAdmin));
        } catch (Exception e) {
            log.error("Error updating admin profile: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("status", false, "message", e.getMessage() != null ? e.getMessage()
                            : "An unexpected error occurred while updating the profile."));
        }
    }

    @PostMapping("/upload_combo_image")
    public ResponseEntity<?> uploadComboImage(
            @RequestPart("comboImage") List<MultipartFile> files,
            @Valid @RequestPart(value = "data", required = false) AdminDto.UploadComboImageRequest request) {
        try {
            Integer adminId = getAdminIdFromAuth();

            String imgName = request != null ? request.getImgName() : null;
            Boolean isFavorite = request != null ? request.getIsFavorite() : false;
            List<String> info = request != null ? request.getInfo() : null;

            List<ComboImage> createdImages = adminService.uploadComboImages(adminId, files, imgName, isFavorite, info);

            return ResponseEntity.status(201).body(Map.of(
                    "status", true,
                    "message", "Combo images uploaded and saved successfully!",
                    "data", createdImages));
        } catch (Exception e) {
            log.error("Error uploading combo images: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("status", false, "message", e.getMessage() != null ? e.getMessage()
                            : "An unexpected error occurred while uploading combo images."));
        }
    }

    @GetMapping("/combo_images")
    public ResponseEntity<?> getAllComboImages(@Valid AdminDto.GetComboImagesRequest request) {
        try {
            if (request == null) {
                request = new AdminDto.GetComboImagesRequest();
            }

            Page<ComboImage> page = adminService.getPaginatedComboImages(request);

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "Combo images fetched successfully");
            response.put("data", page.getContent());
            response.put("pagination",
                    new AdminDto.PaginationInfo(page.getTotalElements(), request.getPage(), request.getLimit()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching combo images: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("status", false, "message", e.getMessage() != null ? e.getMessage()
                            : "An unexpected error occurred while fetching combo images."));
        }
    }

    @PatchMapping("/delete_combo_images")
    public ResponseEntity<?> deleteComboImage(@Valid @RequestBody AdminDto.DeleteComboImageRequest request) {
        try {
            adminService.deleteComboImages(request.getComboImageIds());

            int count = request.getComboImageIds().size();
            String message = count + " " + (count > 1 ? "combo images" : "combo image")
                    + " have been successfully deactivated.";

            return ResponseEntity.ok(Map.of("success", true, "message", message));
        } catch (Exception e) {
            log.error("Error deleting combo images: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage() != null ? e.getMessage()
                            : "Unexpected error occurred while deleting the combo images."));
        }
    }

    @PatchMapping("/combo_images/{id}")
    public ResponseEntity<?> updateComboImage(@PathVariable Integer id,
            @Valid @RequestBody AdminDto.UpdateComboImageRequest request) {
        try {
            ComboImage updatedImage = adminService.updateComboImage(id, request);
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "Combo image updated successfully",
                    "data", updatedImage));
        } catch (Exception e) {
            log.error("Error updating combo image: {}", e.getMessage());
            int status = e.getMessage().contains("not found") ? 404 : 500;
            return ResponseEntity.status(status)
                    .body(Map.of("status", false, "message", e.getMessage() != null ? e.getMessage()
                            : "An unexpected error occurred while updating the combo image."));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody AdminDto.CreateAdminRequest request) {
        try {
            Admin newAdmin = adminService.createAdmin(request);
            return ResponseEntity.status(201).body(Map.of(
                    "status", true,
                    "message", "Admin created successfully.",
                    "data", newAdmin));
        } catch (Exception e) {
            log.error("Error creating admin: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", false, "message", e.getMessage()));
        }
    }
}
