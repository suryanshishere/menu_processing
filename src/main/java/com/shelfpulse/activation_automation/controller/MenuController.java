package com.shelfpulse.activation_automation.controller;

import com.shelfpulse.activation_automation.dto.menu.MenuDto;
import com.shelfpulse.activation_automation.entity.Menu;
import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.service.MenuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/menu")
public class MenuController extends BaseController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping("/processed_menu")
    public ResponseEntity<?> processMenu(@Valid @RequestBody MenuDto.ProcessedMenuRequest request) {
        try {
            List<String> jsonUrls = request.getJsonUrls();

            if (jsonUrls == null || jsonUrls.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                        "status", false,
                        "message", "Request body must include a non-empty 'jsonUrls' array."));
            }

            menuService.processMenu(jsonUrls);

            return ResponseEntity.status(200).body(Map.of(
                    "status", true,
                    "message", "Menu processing initiated successfully."));

        } catch (Exception e) {
            return ResponseEntity.status(200).body(Map.of(
                    "status", false,
                    "message", "Menu processing initiated successfully."));
        }
    }

    @PostMapping(value = "/menu_image/{eateryId}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadMenuImages(
            @PathVariable @Positive(message = "Eatery ID must be a positive integer") Integer eateryId,
            @RequestParam("menuImage") List<MultipartFile> menuImages) {

        try {
            Integer adminId = getAdminIdFromAuth();
            UserType userType = getUserTypeFromAuth();

            Menu menu = menuService.uploadMenuImages(eateryId, adminId, userType, menuImages);

            return ResponseEntity.status(202).body(Map.of(
                    "status", true,
                    "message", "Upload successful. Menu generation is in progress.",
                    "data", Map.of("imageUrls", menu.getRawMenuImgUrls())));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", false,
                    "message",
                    e.getMessage() != null ? e.getMessage() : "Unexpected menu image upload error, try again"));
        }
    }

    @GetMapping("/status/{eateryId}")
    public ResponseEntity<?> getMenuStatus(
            @PathVariable @Positive(message = "Eatery ID must be a positive integer") Integer eateryId) {

        try {
            Integer adminId = getAdminIdFromAuth();
            MenuDto.MenuStatusResponse response = menuService.getMenuStatus(eateryId, adminId);

            if (response.getStatus() == null) {
                return ResponseEntity.status(200).body(Map.of(
                        "status", false,
                        "message", response.getMessage()));
            }

            return ResponseEntity.status(200).body(Map.of(
                    "status", true,
                    "data", response));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", false,
                    "message", "Internal Server Error"));
        }
    }
}
