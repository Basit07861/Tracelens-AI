package com.tracelens.evidence.dto;

import java.time.Instant;

import com.tracelens.evidence.entity.EvidenceFileType;
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
        Instant uploadedAt,
        Instant updatedAt
) {
}