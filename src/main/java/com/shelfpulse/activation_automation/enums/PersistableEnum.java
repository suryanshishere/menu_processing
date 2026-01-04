package com.shelfpulse.activation_automation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public interface PersistableEnum<T> {
    @JsonValue
    T getValue();
}
