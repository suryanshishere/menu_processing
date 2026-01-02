package com.shelfpulse.activation_automation.converter;

import com.shelfpulse.activation_automation.enums.UserType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserTypeConverter implements AttributeConverter<UserType, String> {

    @Override
    public String convertToDatabaseColumn(UserType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public UserType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return UserType.fromValue(dbData);
    }
}
