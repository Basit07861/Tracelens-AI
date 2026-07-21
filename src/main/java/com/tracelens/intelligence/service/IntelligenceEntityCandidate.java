package com.tracelens.intelligence.service;

import java.math.BigDecimal;

import com.tracelens.intelligence.entity.ExtractedEntityType;

public record IntelligenceEntityCandidate(
        ExtractedEntityType entityType,
        String displayValue,
        String normalizedValue,
        String contextSnippet,
        BigDecimal confidence,
        int occurrenceCount,
        Integer firstCharacterOffset,
        Integer lastCharacterOffset
) {
}