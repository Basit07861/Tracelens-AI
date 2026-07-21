package com.tracelens.intelligence.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.tracelens.intelligence.entity.ExtractedEntityType;
import com.tracelens.exception.InvalidRequestException;

@Service
public class IntelligenceEntityNormalizationService {

    public String normalizeDisplayValue(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .replaceAll("\\s{2,}", " ")
                .strip();
    }

    public String normalizeValue(
            ExtractedEntityType type,
            String value
    ) {

        if (type == null) {
            throw new InvalidRequestException(
                    "Entity type is unavailable"
            );
        }

        String displayValue =
                normalizeDisplayValue(value);

        if (displayValue.isBlank()) {
            return "";
        }

        return switch (type) {

            case EMAIL_ADDRESS,
                    PERSON,
                    ORGANIZATION,
                    IP_ADDRESS,
                    DATE,
                    TIME,
                    DATE_TIME ->
                    displayValue.toLowerCase(
                            Locale.ROOT
                    );

            case PHONE_NUMBER ->
                    normalizePhone(displayValue);

            case MONEY ->
                    displayValue
                            .replaceAll("\\s+", "")
                            .replace(",", "")
                            .toLowerCase(Locale.ROOT);

            case URL -> displayValue;
        };
    }

    public String createKey(
            ExtractedEntityType type,
            String normalizedValue
    ) {

        return type.name()
                + "\u0000"
                + normalizedValue;
    }

    private String normalizePhone(
            String value
    ) {

        boolean hasLeadingPlus =
                value.startsWith("+");

        String digits = value.replaceAll(
                "\\D",
                ""
        );

        return hasLeadingPlus
                ? "+" + digits
                : digits;
    }
}