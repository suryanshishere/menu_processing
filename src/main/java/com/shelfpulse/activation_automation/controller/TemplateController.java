package com.shelfpulse.activation_automation.controller;

import com.shelfpulse.activation_automation.dto.template.TemplateDto;
import com.shelfpulse.activation_automation.entity.Menu;
import com.shelfpulse.activation_automation.enums.TemplateType;
import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.service.TemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/template")
public class TemplateController extends BaseController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PatchMapping(value = "/update_template/{templateId}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateTemplate(
            @PathVariable @Positive(message = "Template ID must be a positive integer") Integer templateId,
            @RequestParam(value = "templateMenuImage", required = false) List<MultipartFile> templateMenuImages,
            @Valid @ModelAttribute TemplateDto.UpdateTemplateRequest request) {

        try {
            Integer adminId = getAdminIdFromAuth();

            Menu template = templateService.updateTemplate(templateId, adminId, request, templateMenuImages);

            String pdfUrl = null;
            if (template.getPdfUrls() != null && !template.getPdfUrls().isEmpty()) {
                pdfUrl = template.getPdfUrls().get(template.getPdfUrls().size() - 1);
            }

            return ResponseEntity.status(200).body(Map.of(
                    "status", true,
                    "message", "Template updated successfully.",
                    "data", Map.of("pdfUrl", pdfUrl != null ? pdfUrl : "")));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", false,
                    "message",
                    e.getMessage() != null ? e.getMessage() : "An server error occurred while updating the template."));
        }
    }

    @GetMapping("/get_template/{eateryId}")
    public ResponseEntity<?> getTemplate(
            @PathVariable @Positive(message = "Eatery ID must be a positive integer") Integer eateryId,
            @Valid TemplateDto.GetTemplateRequest request) {

        try {
            Integer adminId = getAdminIdFromAuth();
            UserType userType = getUserTypeFromAuth();

            if (request == null) {
                request = new TemplateDto.GetTemplateRequest();
            }

            Page<Menu> page = templateService.getTemplateMenu(eateryId, request, adminId, userType);

            List<Map<String, Object>> data = page.getContent().stream().map(menu -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", menu.getId());
                item.put("eateryId", menu.getEatery().getId());
                item.put("createdAt", menu.getCreatedAt());
                item.put("updatedAt", menu.getUpdatedAt());
                item.put("status", menu.getStatus());
                item.put("templateName", menu.getTemplateName());
                item.put("imageUrls", menu.getTemplateImgUrls());
                item.put("rawMenuImageUrls", menu.getRawMenuImgUrls());
                item.put("type", menu.getTemplateType());
                item.put("initialJsonUrl", menu.getDataJsonUrl());

                String jsonUrl = null;
                if (menu.getWorkingDataJsonUrls() != null && !menu.getWorkingDataJsonUrls().isEmpty()) {
                    jsonUrl = menu.getWorkingDataJsonUrls().get(menu.getWorkingDataJsonUrls().size() - 1);
                }
                item.put("jsonUrl", jsonUrl);

                String pdfUrl = null;
                if (menu.getPdfUrls() != null && !menu.getPdfUrls().isEmpty()) {
                    pdfUrl = menu.getPdfUrls().get(menu.getPdfUrls().size() - 1);
                }
                item.put("pdfUrl", pdfUrl);

                return item;
            }).collect(Collectors.toList());

            if (data.isEmpty()) {
                return ResponseEntity.status(200).body(Map.of(
                        "status", true,
                        "message", "No template menus found for this eatery.",
                        "data", List.of(),
                        "pagination", Map.of(
                                "totalRecords", 0,
                                "page", request.getPage(),
                                "limit", request.getLimit(),
                                "totalPages", 0)));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "Templates fetched successfully.");
            response.put("data", data);

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("totalRecords", page.getTotalElements());
            pagination.put("page", request.getPage());
            pagination.put("limit", request.getLimit());
            pagination.put("totalPages", page.getTotalPages());
            response.put("pagination", pagination);

            return ResponseEntity.status(200).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", false,
                    "message", "An error occurred while retrieving the template menu."));
        }
    }

    @GetMapping("/get_template_by_type/{type}")
    public ResponseEntity<?> getTemplatesByType(
            @PathVariable TemplateType type,
            @Valid TemplateDto.GetTemplateRequest request) {

        try {
            if (request == null) {
                request = new TemplateDto.GetTemplateRequest();
            }

            Page<Menu> page = templateService.getTemplatesByType(type, request);

            List<List<String>> imageUrlData = page.getContent().stream()
                    .map(Menu::getTemplateImgUrls)
                    .collect(Collectors.toList());

            List<String> flatImageUrls = imageUrlData.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "Templates fetched successfully");
            response.put("data", flatImageUrls);

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
                    "message", e.getMessage() != null ? e.getMessage() : "Server error"));
        }
    }
}
