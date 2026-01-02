package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.entity.Admin;
import com.shelfpulse.activation_automation.repository.AdminRepository;
import com.shelfpulse.activation_automation.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
}
