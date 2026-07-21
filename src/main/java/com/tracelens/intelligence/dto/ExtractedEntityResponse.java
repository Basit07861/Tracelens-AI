package com.tracelens.intelligence.dto;

import java.math.BigDecimal;

import com.tracelens.intelligence.entity.ExtractedEntityType;

public record ExtractedEntityResponse(
        Long entityId,
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