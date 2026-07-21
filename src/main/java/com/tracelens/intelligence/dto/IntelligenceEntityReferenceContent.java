package com.tracelens.intelligence.dto;

import com.tracelens.intelligence.entity.ExtractedEntityType;

public record IntelligenceEntityReferenceContent(
        ExtractedEntityType entityType,
        String value
) {
}