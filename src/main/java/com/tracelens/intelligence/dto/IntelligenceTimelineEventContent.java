package com.tracelens.intelligence.dto;

import java.util.List;

import com.tracelens.intelligence.entity.TimelineEventCertainty;
import com.tracelens.intelligence.entity.TimelineTemporalPrecision;

public record IntelligenceTimelineEventContent(
        String title,
        String description,
        String temporalExpression,
        String normalizedDateTime,
        TimelineTemporalPrecision temporalPrecision,
        TimelineEventCertainty certainty,
        String contextSnippet,
        List<IntelligenceEntityReferenceContent>
                involvedEntities
) {
}