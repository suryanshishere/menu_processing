package com.shelfpulse.activation_automation.security;

import com.shelfpulse.activation_automation.entity.Admin;
import com.shelfpulse.activation_automation.repository.AdminRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public CustomUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String idStr) throws UsernameNotFoundException {
        Integer id = Integer.parseInt(idStr);
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return new AdminUserDetails(
                String.valueOf(admin.getId()),
                admin.getPassword(),
                new ArrayList<>(),
                admin.getUserType());
    }
}
