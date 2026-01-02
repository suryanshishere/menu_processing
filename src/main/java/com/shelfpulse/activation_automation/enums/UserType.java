package com.shelfpulse.activation_automation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserType {
    ADMIN("admin"),
    SUPER_ADMIN("superAdmin");

    private final String value;

    UserType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
