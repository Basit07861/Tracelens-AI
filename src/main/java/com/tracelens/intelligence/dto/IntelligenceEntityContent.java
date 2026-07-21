package com.tracelens.intelligence.dto;

import java.math.BigDecimal;

import com.tracelens.intelligence.entity.ExtractedEntityType;

public record IntelligenceEntityContent(
        ExtractedEntityType entityType,
        String value,
        String contextSnippet,
        BigDecimal confidence
) {
}