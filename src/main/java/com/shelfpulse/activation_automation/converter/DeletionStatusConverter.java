package com.shelfpulse.activation_automation.converter;

import com.shelfpulse.activation_automation.enums.DeletionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeletionStatusConverter implements AttributeConverter<DeletionStatus, String> {

    @Override
    public String convertToDatabaseColumn(DeletionStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue();
    }

    @Override
    public DeletionStatus convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        for (DeletionStatus status : DeletionStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown DeletionStatus value: " + value);
    }
}
