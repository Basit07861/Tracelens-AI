package com.tracelens.ai.service;

import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.evidence.entity.EvidenceIntegrityStatus;
import com.tracelens.evidence.entity.EvidenceStatus;

public record AiEvidenceAnalysisTarget(
        Long evidenceId,
        Long caseId,
        String caseNumber,
        String originalFileName,
        EvidenceFileType fileType,
        EvidenceStatus evidenceStatus,
        EvidenceIntegrityStatus integrityStatus,
        String sourceEvidenceSha256,
        String extractedText
) {
}