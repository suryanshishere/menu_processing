package com.shelfpulse.activation_automation.converter;

import com.shelfpulse.activation_automation.enums.DeletionStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeletionStatusConverter extends AbstractEnumConverter<DeletionStatus> {
    public DeletionStatusConverter() {
        super(DeletionStatus.class);
    }
}
