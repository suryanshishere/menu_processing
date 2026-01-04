package com.shelfpulse.activation_automation.converter;

import com.shelfpulse.activation_automation.enums.MenuStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MenuStatusConverter extends AbstractEnumConverter<MenuStatus> {
    public MenuStatusConverter() {
        super(MenuStatus.class);
    }
}
