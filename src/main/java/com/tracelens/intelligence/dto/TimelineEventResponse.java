package com.tracelens.intelligence.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tracelens.intelligence.entity.TimelineEventCertainty;
import com.tracelens.intelligence.entity.TimelineTemporalPrecision;

public record TimelineEventResponse(
        Long eventId,
        int sequenceNumber,
        String title,
        String description,
        String temporalExpression,
        LocalDateTime normalizedDateTime,
        TimelineTemporalPrecision temporalPrecision,
        TimelineEventCertainty certainty,
        String contextSnippet,
        List<ExtractedEntityResponse> involvedEntities
) {
}