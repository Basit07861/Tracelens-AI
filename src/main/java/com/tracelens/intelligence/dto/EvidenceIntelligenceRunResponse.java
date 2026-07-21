package com.tracelens.intelligence.dto;

import java.time.Instant;
import java.util.List;

import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.intelligence.entity.IntelligenceMethod;
import com.tracelens.intelligence.entity.IntelligenceRunStatus;

public record EvidenceIntelligenceRunResponse(
        Long runId,
        Long evidenceId,
        Long caseId,
        String caseNumber,
        String originalFileName,
        EvidenceFileType fileType,
        Long sourceAnalysisId,
        IntelligenceRunStatus status,
        IntelligenceMethod method,
        boolean humanReviewRequired,
        String provider,
        String model,
        String promptVersion,
        String responseSchemaVersion,
        String sourceEvidenceSha256,
        String sourceTextSha256,
        int entityCount,
        int timelineEventCount,
        String failureMessage,
        List<ExtractedEntityResponse> entities,
        List<TimelineEventResponse> timelineEvents,
        Instant requestedAt,
        Instant startedAt,
        Instant completedAt,
        Instant updatedAt
) {
}