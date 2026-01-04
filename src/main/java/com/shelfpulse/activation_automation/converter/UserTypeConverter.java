package com.shelfpulse.activation_automation.converter;

import com.shelfpulse.activation_automation.enums.UserType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserTypeConverter extends AbstractEnumConverter<UserType> {
    public UserTypeConverter() {
        super(UserType.class);
    }
}
