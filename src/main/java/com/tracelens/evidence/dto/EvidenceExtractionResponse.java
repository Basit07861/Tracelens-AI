package com.tracelens.evidence.dto;

import java.time.Instant;

import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.evidence.entity.EvidenceStatus;

public record EvidenceExtractionResponse(
        Long evidenceId,
        Long caseId,
        String caseNumber,
        String originalFileName,
        EvidenceFileType fileType,
        EvidenceStatus status,
        Integer extractedCharacterCount,
        String extractedText,
        String extractionError,
        Instant processedAt,
        Instant updatedAt
) {
}