package com.tracelens.investigation.dto;

import com.tracelens.investigation.entity.CaseStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateCaseStatusRequest(

        @NotNull(message = "Case status is required")
        CaseStatus status
) {
}