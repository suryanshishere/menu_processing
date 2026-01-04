package com.shelfpulse.activation_automation.controller;

import com.shelfpulse.activation_automation.dto.eatery.EateryDto;
import com.shelfpulse.activation_automation.entity.Eatery;
import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.service.EateryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/eatery")
public class EateryController extends BaseController {

    private final EateryService eateryService;

    public EateryController(EateryService eateryService) {
        this.eateryService = eateryService;
    }

    @PostMapping(value = "/create_eatery", consumes = "multipart/form-data")
    public ResponseEntity<?> createEatery(
            @RequestParam(value = "eateryImage", required = false) MultipartFile eateryImage,
            @RequestParam(value = "eateryLogoImage", required = false) MultipartFile eateryLogoImage,
            @Valid @ModelAttribute EateryDto.CreateRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        System.out.println("=== DEBUG: createEatery called ===");
        System.out.println(
                "eateryImage param: " + (eateryImage != null ? "present, size=" + eateryImage.getSize() : "null"));
        System.out.println("eateryLogoImage param: "
                + (eateryLogoImage != null ? "present, size=" + eateryLogoImage.getSize() : "null"));

        if (httpRequest instanceof org.springframework.web.multipart.MultipartHttpServletRequest multipartRequest) {
            System.out.println("Multipart file names: " + multipartRequest.getFileNames().toString());
            multipartRequest.getFileMap().forEach((name, file) -> {
                System.out.println("  File part: " + name + " -> " + file.getOriginalFilename() + " (" + file.getSize()
                        + " bytes)");
            });
        } else {
            System.out.println("Request is NOT a MultipartHttpServletRequest");
        }

        try {
            Integer adminId = getAdminIdFromAuth();
            Eatery params = eateryService.createEatery(request, adminId, eateryImage, eateryLogoImage);
            return ResponseEntity.status(201).body(Map.of(
                    "message", "Eatery created successfully!",
                    "data", params));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred while creating the eatery."));
        }
    }

    @GetMapping("/get_eatery/{eateryId}")
    public ResponseEntity<?> getEatery(@PathVariable Integer eateryId) {
        try {
            Integer adminId = getAdminIdFromAuth();
            UserType userType = getUserTypeFromAuth();
            Eatery eatery = eateryService.getEatery(eateryId, adminId, userType);
            if (eatery == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Eatery not found!"));
            }
            return ResponseEntity.ok(eatery);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/get_all_eatery")
    public ResponseEntity<?> getAllEatery(@Valid EateryDto.GetAllRequest request) {
        try {
            Integer adminId = getAdminIdFromAuth();
            UserType userType = getUserTypeFromAuth();

            if (request == null)
                request = new EateryDto.GetAllRequest();

            Page<Eatery> page = eateryService.getAllEateries(request, adminId, userType);

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "Eateries fetched successfully");
            response.put("data", page.getContent());

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("total", page.getTotalElements());
            pagination.put("page", request.getPage());
            pagination.put("limit", request.getLimit());
            pagination.put("totalPages", page.getTotalPages());
            response.put("pagination", pagination);

            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", false,
                    "message",
                    e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while fetching eateries."));
        }
    }

    @PatchMapping(value = "/update_eatery/{eateryId}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateEateryMultipart(
            @PathVariable Integer eateryId,
            @RequestParam(value = "eateryImage", required = false) MultipartFile eateryImage,
            @RequestParam(value = "eateryLogoImage", required = false) MultipartFile eateryLogoImage,
            @Valid @ModelAttribute EateryDto.UpdateRequest request) {

        try {
            Integer adminId = getAdminIdFromAuth();
            Eatery eatery = eateryService.updateEatery(eateryId, request, adminId, eateryImage, eateryLogoImage);
            return ResponseEntity.ok(Map.of(
                    "message", "Eatery updated successfully!",
                    "data", eatery));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping(value = "/update_eatery/{eateryId}", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateEateryJson(
            @PathVariable Integer eateryId,
            @Valid @RequestBody EateryDto.UpdateRequest request) {

        try {
            Integer adminId = getAdminIdFromAuth();
            Eatery eatery = eateryService.updateEatery(eateryId, request, adminId, null, null);
            return ResponseEntity.ok(Map.of(
                    "message", "Eatery updated successfully!",
                    "data", eatery));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/delete_eateries")
    public ResponseEntity<?> deleteEateries(@Valid @RequestBody EateryDto.DeleteRequest request) {
        try {
            Integer adminId = getAdminIdFromAuth();
            UserType userType = getUserTypeFromAuth();
            eateryService.deleteEateries(request.getEateryIds(), adminId, userType);
            return ResponseEntity.status(200).body(Map.of(
                    "success", true,
                    "message",
                    request.getEateryIds().size() + " " + (request.getEateryIds().size() > 1 ? "Eateries" : "Eatery")
                            + " have been successfully deleted."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message",
                    e.getMessage() != null ? e.getMessage() : "An server error occurred while deleting the eateries."));
        }
    }
}
