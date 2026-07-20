package com.tracelens.ai.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.tracelens.ai.config.AiAnalysisProperties;
import com.tracelens.ai.dto.AiEvidenceAnalysisContent;
import com.tracelens.ai.entity.AiPreviewRiskLevel;
import com.tracelens.exception.AiResponseValidationException;

@Component
public class AiEvidenceAnalysisValidator {

    private final AiAnalysisProperties properties;

    public AiEvidenceAnalysisValidator(
            AiAnalysisProperties properties
    ) {
        this.properties = properties;
    }

    public AiEvidenceAnalysisContent validate(
            AiEvidenceAnalysisContent analysis
    ) {

        if (analysis == null) {
            throw new AiResponseValidationException(
                    "The AI analysis response was empty"
            );
        }

        String summary = normalizeRequiredText(
                analysis.summary(),
                "The AI analysis did not contain a summary"
        );

        enforceMaximumLength(
                summary,
                properties.getMaxSummaryCharacters(),
                "The AI analysis summary exceeds "
                + "the allowed length"
        );

        AiPreviewRiskLevel riskLevel =
                analysis.riskLevel();

        if (riskLevel == null) {
            throw new AiResponseValidationException(
                    "The AI analysis did not contain "
                    + "a valid risk level"
            );
        }

        if (analysis.sufficientInformation() == null) {
            throw new AiResponseValidationException(
                    "The AI analysis did not indicate "
                    + "whether the evidence was sufficient"
            );
        }

        List<String> findings = normalizeList(
                analysis.suspiciousFindings(),
                properties.getMaxFindings(),
                "suspicious findings"
        );

        List<String> actions = normalizeList(
                analysis.recommendedActions(),
                properties.getMaxActions(),
                "recommended actions"
        );

        List<String> limitations = normalizeList(
                analysis.limitations(),
                properties.getMaxLimitations(),
                "limitations"
        );

        if (actions.isEmpty()) {
            throw new AiResponseValidationException(
                    "The AI analysis did not contain "
                    + "a recommended investigative action"
            );
        }

        boolean sufficientInformation =
                analysis.sufficientInformation();

        if (!sufficientInformation
                && limitations.isEmpty()) {

            throw new AiResponseValidationException(
                    "The AI analysis marked the evidence "
                    + "as insufficient but did not explain "
                    + "the limitations"
            );
        }

        boolean elevatedRisk =
                riskLevel == AiPreviewRiskLevel.HIGH
                || riskLevel
                        == AiPreviewRiskLevel.CRITICAL;

        if (elevatedRisk && findings.isEmpty()) {
            throw new AiResponseValidationException(
                    "A HIGH or CRITICAL analysis must "
                    + "include an evidence-supported finding"
            );
        }

        return new AiEvidenceAnalysisContent(
                summary,
                riskLevel,
                findings,
                actions,
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
                    "The AI analysis contains too many "
                    + fieldName
            );
        }

        for (String value : normalizedValues) {
            enforceMaximumLength(
                    value,
                    properties.getMaxItemCharacters(),
                    "An AI analysis "
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