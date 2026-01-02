package com.shelfpulse.activation_automation.controller;

import com.shelfpulse.activation_automation.dto.auth.AuthDto;
import com.shelfpulse.activation_automation.service.AdminService;
import com.shelfpulse.activation_automation.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    private final AdminService adminService;
    private final AuthService authService;

    public AuthController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request) {
        String usernameOrEmail = request.getUsername() != null ? request.getUsername() : request.getEmail();
        if (usernameOrEmail == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username or Email is required"));
        }

        log.info("Attempting login for: {}", usernameOrEmail);

        Map<String, Object> result = adminService.login(usernameOrEmail, request.getPassword());
        if (result == null) {
            log.warn("Login failed for: {}", usernameOrEmail);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password!"));
        }

        // Create a new map to ensure we control the response structure
        Map<String, Object> response = new HashMap<>(result);
        response.put("message", "logged in!");

        log.info("Login successful for: {}. Response keys: {}", usernameOrEmail, response.keySet());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forget_password")
    public ResponseEntity<?> forgetPassword(@RequestBody AuthDto.ForgotPasswordRequest request) {
        String usernameOrEmail = request.getUsername() != null ? request.getUsername() : request.getEmail();
        if (usernameOrEmail == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username or Email is required"));
        }

        try {
            Map<String, Object> data = authService.forgotPassword(usernameOrEmail);
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "OTP sent to email!",
                    "email", data.get("email")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("status", false, "message", e.getMessage() != null ? e.getMessage() : "Unexpected error"));
        }
    }

    @PostMapping("/update_forgot_password")
    public ResponseEntity<?> updateForgotPassword(@RequestBody AuthDto.UpdateForgotPasswordRequest request) {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();

            String adminIdStr = authentication.getName();
            Integer adminId = Integer.parseInt(adminIdStr);

            authService.updateForgotPassword(request.getOtp(), request.getOtpVerificationHash(), request.getPassword(),
                    adminId);

            return ResponseEntity.ok(Map.of("status", true, "message", "Password updated successfully!"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", false, "message", e.getMessage()));
        }
    }
}
