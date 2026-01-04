package com.shelfpulse.activation_automation.security;

import com.shelfpulse.activation_automation.enums.UserType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AdminUserDetails extends User {

    private final UserType userType;

    public AdminUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
            UserType userType) {
        super(username, password, authorities);
        this.userType = userType;
    }

    public UserType getUserType() {
        return userType;
    }
}
