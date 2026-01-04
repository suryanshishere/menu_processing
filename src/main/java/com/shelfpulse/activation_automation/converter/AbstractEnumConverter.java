package com.shelfpulse.activation_automation.converter;

import com.shelfpulse.activation_automation.enums.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import java.util.Arrays;

public abstract class AbstractEnumConverter<E extends Enum<E> & PersistableEnum<String>>
        implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    protected AbstractEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown " + enumClass.getSimpleName() + " value: " + dbData));
    }
}
