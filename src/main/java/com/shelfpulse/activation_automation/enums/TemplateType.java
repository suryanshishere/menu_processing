package com.shelfpulse.activation_automation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TemplateType implements PersistableEnum<String> {
    SPIRAL("spiral"),
    CARD("card"),
    WALL("wall");

    private final String value;

    TemplateType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
