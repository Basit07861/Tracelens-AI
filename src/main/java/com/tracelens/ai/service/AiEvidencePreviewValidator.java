package com.tracelens.ai.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.tracelens.ai.config.AiPreviewProperties;
import com.tracelens.ai.dto.AiEvidencePreviewContent;
import com.tracelens.exception.AiResponseValidationException;

@Component
public class AiEvidencePreviewValidator {

    private final AiPreviewProperties properties;

    public AiEvidencePreviewValidator(
            AiPreviewProperties properties
    ) {
        this.properties = properties;
    }

    public AiEvidencePreviewContent validate(
            AiEvidencePreviewContent preview
    ) {

        if (preview == null) {
            throw new AiResponseValidationException(
                    "The AI response was empty"
            );
        }

        String summary = normalizeRequiredText(
                preview.summary(),
                "The AI response did not contain a summary"
        );

        enforceMaximumLength(
                summary,
                properties.getMaxSummaryCharacters(),
                "The AI summary exceeds the allowed length"
        );

        if (preview.riskLevel() == null) {
            throw new AiResponseValidationException(
                    "The AI response did not contain "
                    + "a valid risk level"
            );
        }

        if (preview.sufficientInformation() == null) {
            throw new AiResponseValidationException(
                    "The AI response did not indicate "
                    + "whether the evidence was sufficient"
            );
        }

        List<String> keyIndicators = normalizeList(
                preview.keyIndicators(),
                properties.getMaxIndicators(),
                "key indicators"
        );

        List<String> limitations = normalizeList(
                preview.limitations(),
                properties.getMaxLimitations(),
                "limitations"
        );

        boolean sufficientInformation =
                preview.sufficientInformation();

        if (
                !sufficientInformation
                && limitations.isEmpty()
        ) {
            throw new AiResponseValidationException(
                    "The AI response marked the evidence "
                    + "as insufficient but provided "
                    + "no limitations"
            );
        }

        return new AiEvidencePreviewContent(
                summary,
                preview.riskLevel(),
                keyIndicators,
                sufficientInformation,
                limitations
        );
    }

    private List<String> normalizeList(
            List<String> values,
            int maximumItems,
            String fieldName
    ) {

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<String> normalizedValues = values.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeInlineText)
                .filter(value -> !value.isBlank())
                .toList();

        if (normalizedValues.size() > maximumItems) {
            throw new AiResponseValidationException(
                    "The AI response contains too many "
                    + fieldName
            );
        }

        for (String value : normalizedValues) {
            enforceMaximumLength(
                    value,
                    properties
                            .getMaxListItemCharacters(),
                    "An AI "
                    + fieldName
                    + " item exceeds the allowed length"
            );
        }

        return List.copyOf(normalizedValues);
    }

    private String normalizeRequiredText(
            String value,
            String errorMessage
    ) {

        String normalizedValue =
                normalizeInlineText(value);

        if (normalizedValue.isBlank()) {
            throw new AiResponseValidationException(
                    errorMessage
            );
        }

        return normalizedValue;
    }

    private String normalizeInlineText(
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

    private void enforceMaximumLength(
            String value,
            int maximumLength,
            String errorMessage
    ) {

        if (value.length() > maximumLength) {
            throw new AiResponseValidationException(
                    errorMessage
            );
        }
    }
}