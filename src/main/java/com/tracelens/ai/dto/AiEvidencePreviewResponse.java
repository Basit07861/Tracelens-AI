package com.tracelens.ai.dto;

import java.time.Instant;
import java.util.List;

import com.tracelens.ai.entity.AiPreviewRiskLevel;
import com.tracelens.evidence.entity.EvidenceFileType;

public record AiEvidencePreviewResponse(
        Long evidenceId,
        Long caseId,
        String caseNumber,
        String originalFileName,
        EvidenceFileType fileType,
        String summary,
        AiPreviewRiskLevel riskLevel,
        List<String> keyIndicators,
        boolean sufficientInformation,
        List<String> limitations,
        boolean humanReviewRequired,
        Instant generatedAt
) {
}