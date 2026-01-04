package com.shelfpulse.activation_automation.converter;

import com.shelfpulse.activation_automation.enums.TemplateType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TemplateTypeConverter extends AbstractEnumConverter<TemplateType> {
    public TemplateTypeConverter() {
        super(TemplateType.class);
    }
}
