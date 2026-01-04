package com.shelfpulse.activation_automation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserType implements PersistableEnum<String> {
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

    public static UserType fromValue(String value) {
        for (UserType type : UserType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown UserType value: " + value);
    }
}
