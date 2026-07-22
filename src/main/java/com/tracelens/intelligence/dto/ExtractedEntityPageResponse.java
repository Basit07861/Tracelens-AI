package com.tracelens.intelligence.dto;

import java.util.List;

import com.tracelens.intelligence.entity.ExtractedEntityType;

public record ExtractedEntityPageResponse(

        Long runId,
        Long evidenceId,

        ExtractedEntityType entityTypeFilter,

        List<ExtractedEntityResponse> content,

        int page,
        int size,

        long totalElements,
        int totalPages,
        int numberOfElements,

        boolean first,
        boolean last
) {
}