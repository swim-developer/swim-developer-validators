package com.github.swim_developer.validator.consumer.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Converter(autoApply = true)
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final String DELIMITER = ",";

    private static final Pattern DELIMITER_PATTERN = Pattern.compile(",");

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(DELIMITER, list);
    }

    @Override
    public List<String> convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(DELIMITER_PATTERN.split(value, 0))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
