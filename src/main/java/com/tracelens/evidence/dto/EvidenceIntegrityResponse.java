package com.tracelens.evidence.dto;

import java.time.Instant;

import com.tracelens.evidence.entity.EvidenceIntegrityStatus;

public record EvidenceIntegrityResponse(
        Long evidenceId,
        Long caseId,
        String caseNumber,
        String originalFileName,
        String expectedSha256Hash,
        String currentSha256Hash,
        boolean matches,
        EvidenceIntegrityStatus integrityStatus,
        Instant verifiedAt
) {
}