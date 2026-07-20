package com.tracelens.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai.analysis")
public class AiAnalysisProperties {

    private int maxInputCharacters = 30000;

    private int maxSummaryCharacters = 2000;

    private int maxFindings = 8;

    private int maxActions = 8;

    private int maxLimitations = 6;

    private int maxItemCharacters = 500;

    private int validationAttempts = 2;

    private int maxFailureMessageCharacters = 1000;

    private String promptVersion =
            "evidence-analysis-v1";

    private String responseSchemaVersion =
            "ai-analysis-v1";

    public int getMaxInputCharacters() {
        return maxInputCharacters;
    }

    public void setMaxInputCharacters(
            int maxInputCharacters
    ) {
        this.maxInputCharacters = requirePositive(
                maxInputCharacters,
                "Maximum analysis input characters"
        );
    }

    public int getMaxSummaryCharacters() {
        return maxSummaryCharacters;
    }

    public void setMaxSummaryCharacters(
            int maxSummaryCharacters
    ) {
        this.maxSummaryCharacters = requirePositive(
                maxSummaryCharacters,
                "Maximum analysis summary characters"
        );
    }

    public int getMaxFindings() {
        return maxFindings;
    }

    public void setMaxFindings(
            int maxFindings
    ) {
        this.maxFindings = requirePositive(
                maxFindings,
                "Maximum analysis findings"
        );
    }

    public int getMaxActions() {
        return maxActions;
    }

    public void setMaxActions(
            int maxActions
    ) {
        this.maxActions = requirePositive(
                maxActions,
                "Maximum analysis actions"
        );
    }

    public int getMaxLimitations() {
        return maxLimitations;
    }

    public void setMaxLimitations(
            int maxLimitations
    ) {
        this.maxLimitations = requirePositive(
                maxLimitations,
                "Maximum analysis limitations"
        );
    }

    public int getMaxItemCharacters() {
        return maxItemCharacters;
    }

    public void setMaxItemCharacters(
            int maxItemCharacters
    ) {
        this.maxItemCharacters = requirePositive(
                maxItemCharacters,
                "Maximum analysis list-item characters"
        );
    }

    public int getValidationAttempts() {
        return validationAttempts;
    }

    public void setValidationAttempts(
            int validationAttempts
    ) {
        this.validationAttempts = requirePositive(
                validationAttempts,
                "Analysis validation attempts"
        );
    }

    public int getMaxFailureMessageCharacters() {
        return maxFailureMessageCharacters;
    }

    public void setMaxFailureMessageCharacters(
            int maxFailureMessageCharacters
    ) {
        this.maxFailureMessageCharacters =
                requirePositive(
                        maxFailureMessageCharacters,
                        "Maximum failure-message characters"
                );
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(
            String promptVersion
    ) {
        this.promptVersion = requireText(
                promptVersion,
                "Analysis prompt version"
        );
    }

    public String getResponseSchemaVersion() {
        return responseSchemaVersion;
    }

    public void setResponseSchemaVersion(
            String responseSchemaVersion
    ) {
        this.responseSchemaVersion = requireText(
                responseSchemaVersion,
                "Analysis response-schema version"
        );
    }

    private int requirePositive(
            int value,
            String propertyName
    ) {

        if (value <= 0) {
            throw new IllegalArgumentException(
                    propertyName
                    + " must be greater than zero"
            );
        }

        return value;
    }

    private String requireText(
            String value,
            String propertyName
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    propertyName
                    + " cannot be blank"
            );
        }

        return value.strip();
    }
}