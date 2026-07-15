package com.tracelens.investigation.dto;

import com.tracelens.investigation.entity.CasePriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCaseRequest(

        @NotBlank(message = "Case title is required")
        @Size(
                min = 5,
                max = 150,
                message = "Case title must contain between 5 and 150 characters"
        )
        String title,

        @NotBlank(message = "Case description is required")
        @Size(
                min = 10,
                max = 5000,
                message = "Case description must contain between 10 and 5000 characters"
        )
        String description,

        CasePriority priority
) {
}