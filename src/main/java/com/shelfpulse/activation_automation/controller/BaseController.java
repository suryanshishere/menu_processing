package com.shelfpulse.activation_automation.controller;

import com.shelfpulse.activation_automation.enums.UserType;
import com.shelfpulse.activation_automation.security.AdminUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {

    protected Integer getAdminIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Integer.parseInt(auth.getName());
    }

    protected UserType getUserTypeFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof AdminUserDetails) {
            return ((AdminUserDetails) principal).getUserType();
        }
        return UserType.ADMIN;
    }
}
