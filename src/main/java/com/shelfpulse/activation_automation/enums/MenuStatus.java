package com.shelfpulse.activation_automation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MenuStatus implements PersistableEnum<String> {
    PROCESSING("processing"),
    GENERATED("generated"),
    COMPLETED("completed"),
    FAILED("failed"),
    INACTIVE("inactive");

    private final String value;

    MenuStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
