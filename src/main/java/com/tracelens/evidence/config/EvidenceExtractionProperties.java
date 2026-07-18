package com.tracelens.evidence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "app.evidence.extraction"
)
public class EvidenceExtractionProperties {

    private int maxCharacters = 100000;

    private int maxPdfPages = 100;

    private int maxCsvRows = 5000;

    private int maxJsonDepth = 50;

    private int maxErrorMessageLength = 1000;

    public int getMaxCharacters() {
        return maxCharacters;
    }

    public void setMaxCharacters(
            int maxCharacters
    ) {
        this.maxCharacters = requirePositive(
                maxCharacters,
                "Maximum extracted characters"
        );
    }

    public int getMaxPdfPages() {
        return maxPdfPages;
    }

    public void setMaxPdfPages(
            int maxPdfPages
    ) {
        this.maxPdfPages = requirePositive(
                maxPdfPages,
                "Maximum PDF pages"
        );
    }

    public int getMaxCsvRows() {
        return maxCsvRows;
    }

    public void setMaxCsvRows(
            int maxCsvRows
    ) {
        this.maxCsvRows = requirePositive(
                maxCsvRows,
                "Maximum CSV rows"
        );
    }

    public int getMaxJsonDepth() {
        return maxJsonDepth;
    }

    public void setMaxJsonDepth(
            int maxJsonDepth
    ) {
        this.maxJsonDepth = requirePositive(
                maxJsonDepth,
                "Maximum JSON depth"
        );
    }

    public int getMaxErrorMessageLength() {
        return maxErrorMessageLength;
    }

    public void setMaxErrorMessageLength(
            int maxErrorMessageLength
    ) {
        this.maxErrorMessageLength = requirePositive(
                maxErrorMessageLength,
                "Maximum extraction error length"
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