package com.tracelens.intelligence.dto;

import java.time.Instant;

import com.tracelens.intelligence.entity.IntelligenceMethod;
import com.tracelens.intelligence.entity.IntelligenceRunStatus;

public record EvidenceIntelligenceRunSummaryResponse(

        Long runId,
        Long evidenceId,
        Long sourceAnalysisId,

        IntelligenceRunStatus status,
        IntelligenceMethod method,

        boolean humanReviewRequired,

        String provider,
        String model,
        String promptVersion,
        String responseSchemaVersion,

        int entityCount,
        int timelineEventCount,

        String failureMessage,

        Instant requestedAt,
        Instant startedAt,
        Instant completedAt,
        Instant updatedAt
) {
}