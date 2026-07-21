package com.tracelens.intelligence.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tracelens.intelligence.dto.IntelligenceEntityReferenceContent;
import com.tracelens.intelligence.entity.TimelineEventCertainty;
import com.tracelens.intelligence.entity.TimelineTemporalPrecision;

public record IntelligenceTimelineCandidate(
        int sequenceNumber,
        String title,
        String description,
        String temporalExpression,
        LocalDateTime normalizedDateTime,
        TimelineTemporalPrecision temporalPrecision,
        TimelineEventCertainty certainty,
        String contextSnippet,
        List<IntelligenceEntityReferenceContent>
                involvedEntities
) {
}