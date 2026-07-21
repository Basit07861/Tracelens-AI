package com.tracelens.intelligence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "app.intelligence.extraction"
)
public class IntelligenceExtractionProperties {

    private int maxInputCharacters = 30000;

    private int maxAiEntities = 60;

    private int maxTotalEntities = 100;

    private int maxTimelineEvents = 20;

    private int maxValueCharacters = 500;

    private int maxContextCharacters = 1000;

    private int maxTitleCharacters = 300;

    private int maxDescriptionCharacters = 2000;

    private int maxEventEntityLinks = 12;

    private int validationAttempts = 2;

    private int maxFailureMessageCharacters = 1000;

    private String promptVersion =
            "evidence-intelligence-v1";

    private String responseSchemaVersion =
            "evidence-intelligence-v1";

    public int getMaxInputCharacters() {
        return maxInputCharacters;
    }

    public void setMaxInputCharacters(
            int maxInputCharacters
    ) {
        this.maxInputCharacters = requirePositive(
                maxInputCharacters,
                "Maximum intelligence input characters"
        );
    }

    public int getMaxAiEntities() {
        return maxAiEntities;
    }

    public void setMaxAiEntities(
            int maxAiEntities
    ) {
        this.maxAiEntities = requirePositive(
                maxAiEntities,
                "Maximum AI entity count"
        );
    }

    public int getMaxTotalEntities() {
        return maxTotalEntities;
    }

    public void setMaxTotalEntities(
            int maxTotalEntities
    ) {
        this.maxTotalEntities = requirePositive(
                maxTotalEntities,
                "Maximum total entity count"
        );
    }

    public int getMaxTimelineEvents() {
        return maxTimelineEvents;
    }

    public void setMaxTimelineEvents(
            int maxTimelineEvents
    ) {
        this.maxTimelineEvents = requirePositive(
                maxTimelineEvents,
                "Maximum timeline-event count"
        );
    }

    public int getMaxValueCharacters() {
        return maxValueCharacters;
    }

    public void setMaxValueCharacters(
            int maxValueCharacters
    ) {
        this.maxValueCharacters = requirePositive(
                maxValueCharacters,
                "Maximum entity-value characters"
        );
    }

    public int getMaxContextCharacters() {
        return maxContextCharacters;
    }

    public void setMaxContextCharacters(
            int maxContextCharacters
    ) {
        this.maxContextCharacters = requirePositive(
                maxContextCharacters,
                "Maximum context characters"
        );
    }

    public int getMaxTitleCharacters() {
        return maxTitleCharacters;
    }

    public void setMaxTitleCharacters(
            int maxTitleCharacters
    ) {
        this.maxTitleCharacters = requirePositive(
                maxTitleCharacters,
                "Maximum timeline-title characters"
        );
    }

    public int getMaxDescriptionCharacters() {
        return maxDescriptionCharacters;
    }

    public void setMaxDescriptionCharacters(
            int maxDescriptionCharacters
    ) {
        this.maxDescriptionCharacters =
                requirePositive(
                        maxDescriptionCharacters,
                        "Maximum timeline-description characters"
                );
    }

    public int getMaxEventEntityLinks() {
        return maxEventEntityLinks;
    }

    public void setMaxEventEntityLinks(
            int maxEventEntityLinks
    ) {
        this.maxEventEntityLinks = requirePositive(
                maxEventEntityLinks,
                "Maximum event entity-link count"
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
                "Intelligence validation attempts"
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
                "Intelligence prompt version"
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
                "Intelligence response-schema version"
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