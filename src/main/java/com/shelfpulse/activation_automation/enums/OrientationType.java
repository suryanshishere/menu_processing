package com.shelfpulse.activation_automation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrientationType implements PersistableEnum<String> {
    LANDSCAPE("landscape"),
    PORTRAIT("portrait");

    private final String value;

    OrientationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
