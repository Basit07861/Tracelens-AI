package com.tracelens.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai.preview")
public class AiPreviewProperties {

    private int maxInputCharacters = 30000;

    private int maxSummaryCharacters = 1200;

    private int maxIndicators = 6;

    private int maxLimitations = 5;

    private int maxListItemCharacters = 300;

    private int validationAttempts = 2;

    public int getMaxInputCharacters() {
        return maxInputCharacters;
    }

    public void setMaxInputCharacters(
            int maxInputCharacters
    ) {
        this.maxInputCharacters = requirePositive(
                maxInputCharacters,
                "Maximum AI input characters"
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
                "Maximum AI summary characters"
        );
    }

    public int getMaxIndicators() {
        return maxIndicators;
    }

    public void setMaxIndicators(
            int maxIndicators
    ) {
        this.maxIndicators = requirePositive(
                maxIndicators,
                "Maximum AI indicator count"
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
                "Maximum AI limitation count"
        );
    }

    public int getMaxListItemCharacters() {
        return maxListItemCharacters;
    }

    public void setMaxListItemCharacters(
            int maxListItemCharacters
    ) {
        this.maxListItemCharacters = requirePositive(
                maxListItemCharacters,
                "Maximum AI list-item characters"
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
                "AI response validation attempts"
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
}