package com.tracelens.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNoteRequest(

        @NotBlank(
                message = "Note content is required"
        )
        @Size(
                max = 5000,
                message = "Note content cannot exceed 5000 characters"
        )
        String content,

        boolean pinned
) {
}