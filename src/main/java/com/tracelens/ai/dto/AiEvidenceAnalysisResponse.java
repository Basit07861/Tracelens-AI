package com.tracelens.ai.dto;

import java.time.Instant;
import java.util.List;

import com.tracelens.ai.entity.AiAnalysisStatus;
import com.tracelens.ai.entity.AiPreviewRiskLevel;
import com.tracelens.evidence.entity.EvidenceFileType;

public record AiEvidenceAnalysisResponse(
        Long analysisId,
        Long evidenceId,
        Long caseId,
        String caseNumber,
        String originalFileName,
        EvidenceFileType fileType,
        AiAnalysisStatus status,
        String summary,
        AiPreviewRiskLevel riskLevel,
        List<String> suspiciousFindings,
        List<String> recommendedActions,
        Boolean sufficientInformation,
        List<String> limitations,
        boolean humanReviewRequired,
        String provider,
        String model,
        String promptVersion,
        String responseSchemaVersion,
        String sourceEvidenceSha256,
        String sourceTextSha256,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        String failureMessage,
        Instant requestedAt,
        Instant startedAt,
        Instant completedAt,
        Instant updatedAt
) {
}