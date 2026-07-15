package com.tracelens.investigation.dto;

import java.time.Instant;

import com.tracelens.investigation.entity.CasePriority;
import com.tracelens.investigation.entity.CaseStatus;

public record CaseResponse(
        Long id,
        String caseNumber,
        String title,
        String description,
        CaseStatus status,
        CasePriority priority,
        Long ownerId,
        String ownerName,
        String ownerEmail,
        Instant createdAt,
        Instant updatedAt
) {
}