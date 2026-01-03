package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.dto.email.OtpMailData;
import com.shelfpulse.activation_automation.entity.Admin;
import com.shelfpulse.activation_automation.enums.MailType;
import com.shelfpulse.activation_automation.repository.AdminRepository;
import com.shelfpulse.activation_automation.util.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static final int FORGOT_PASSWORD_OTP_EXPIRY_MINUTES = 10;

    private final AdminRepository adminRepository;
    private final MailService mailService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(AdminRepository adminRepository, MailService mailService, StringRedisTemplate redisTemplate,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.mailService = mailService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> forgotPassword(String emailOrUsername) {
        Admin admin = adminRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        int otp = 100000 + new Random().nextInt(900000);
        String otpStr = String.valueOf(otp);
        String otpVerificationHash = passwordEncoder.encode("admin:" + admin.getId() + "_" + otpStr);

        String key = "FORGOT_PASSWORD:" + admin.getId();
        redisTemplate.opsForValue().set(key, otpStr, FORGOT_PASSWORD_OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);

        OtpMailData mailData = new OtpMailData(MailType.RESET_PASSWORD, otpStr, FORGOT_PASSWORD_OTP_EXPIRY_MINUTES,
                admin.getUsername());
        String emailSubject = "Password Reset OTP: [" + otp + "]";
        mailService.sendMail(admin.getEmail(), emailSubject, "otpEmailTemplate", mailData, null, null, null);

        String token = jwtUtil.generateToken(admin);

        Map<String, Object> response = new HashMap<>();
        response.put("otpVerificationHash", otpVerificationHash);
        response.put("token", token);
        response.put("expiryIn", FORGOT_PASSWORD_OTP_EXPIRY_MINUTES);
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
