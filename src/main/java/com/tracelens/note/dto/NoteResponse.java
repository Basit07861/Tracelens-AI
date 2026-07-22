package com.tracelens.note.dto;

import java.time.Instant;

public record NoteResponse(

        Long noteId,
        Long caseId,

        Long authorId,
        String authorName,

        String content,
        boolean pinned,

        Instant createdAt,
        Instant updatedAt
) {
}