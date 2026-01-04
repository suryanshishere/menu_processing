package com.shelfpulse.activation_automation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MailType implements PersistableEnum<String> {
    RESET_PASSWORD("reset_password"),
    PROFILE_UPDATE("profile_update");

    private final String value;

    MailType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
