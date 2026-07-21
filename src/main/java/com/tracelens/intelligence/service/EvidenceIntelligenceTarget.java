package com.tracelens.intelligence.service;

import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.evidence.entity.EvidenceStatus;

public record EvidenceIntelligenceTarget(
        Long evidenceId,
        Long caseId,
        String caseNumber,
        String originalFileName,
        EvidenceFileType fileType,
        EvidenceStatus evidenceStatus,
        String sourceEvidenceSha256,
        String extractedText
) {
}