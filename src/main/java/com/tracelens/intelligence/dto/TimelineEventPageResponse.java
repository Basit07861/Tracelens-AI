package com.tracelens.intelligence.dto;

import java.util.List;

import com.tracelens.intelligence.entity.TimelineEventCertainty;
import com.tracelens.intelligence.entity.TimelineTemporalPrecision;

public record TimelineEventPageResponse(

        Long runId,
        Long evidenceId,

        TimelineEventCertainty certaintyFilter,

        TimelineTemporalPrecision temporalPrecisionFilter,

        List<TimelineEventResponse> content,

        int page,
        int size,

        long totalElements,
        int totalPages,
        int numberOfElements,

        boolean first,
        boolean last
) {
}