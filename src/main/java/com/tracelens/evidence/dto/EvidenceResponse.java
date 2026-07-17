package com.tracelens.evidence.dto;

import java.time.Instant;

import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.evidence.entity.EvidenceIntegrityStatus;
import com.tracelens.evidence.entity.EvidenceStatus;

public record EvidenceResponse(
        Long id,
        Long caseId,
        String caseNumber,
        String originalFileName,
        EvidenceFileType fileType,
        String contentType,
        long fileSizeBytes,
        String description,
        EvidenceStatus status,
        String sha256Hash,
        EvidenceIntegrityStatus integrityStatus,
        Instant lastIntegrityVerifiedAt,
        Instant uploadedAt,
        Instant updatedAt
) {
}