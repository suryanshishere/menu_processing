package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.dto.email.OtpMailData;
import com.shelfpulse.activation_automation.entity.Admin;
import com.shelfpulse.activation_automation.enums.MailType;
import com.shelfpulse.activation_automation.repository.AdminRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final MailService mailService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AdminRepository adminRepository, MailService mailService, StringRedisTemplate redisTemplate,
            PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.mailService = mailService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> forgotPassword(String emailOrUsername) {
        Admin admin = adminRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = String.format("%04d", new Random().nextInt(10000));
        String key = "FORGOT_PASSWORD:" + admin.getId(); // Key stored in redis

        // Save OTP to Redis with expiration (e.g., 10 minutes)
        redisTemplate.opsForValue().set(key, otp, 10, TimeUnit.MINUTES);

        OtpMailData mailData = new OtpMailData(MailType.RESET_PASSWORD, otp, 10, admin.getUsername());
        mailService.sendMail(admin.getEmail(), "Password Reset Request", "otpEmailTemplate", mailData, null, null,
                null);

        Map<String, Object> response = new HashMap<>();
        response.put("email", admin.getEmail());
        return response;
    }

    public void updateForgotPassword(String otp, String otpVerificationHash, String newPassword, Integer adminId) {

        String key = "FORGOT_PASSWORD:" + adminId;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);

        redisTemplate.delete(key);
    }
}
