package com.shelfpulse.activation_automation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DeletionStatus {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    DeletionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
