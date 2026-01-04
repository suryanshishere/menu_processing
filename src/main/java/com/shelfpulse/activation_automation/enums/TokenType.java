package com.shelfpulse.activation_automation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TokenType implements PersistableEnum<String> {
    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
