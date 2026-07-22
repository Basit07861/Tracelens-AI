package com.tracelens.intelligence.dto;

import java.util.List;

public record EvidenceIntelligenceRunHistoryResponse(

        Long evidenceId,

        List<EvidenceIntelligenceRunSummaryResponse> content,

        int page,
        int size,

        long totalElements,
        int totalPages,
        int numberOfElements,

        boolean first,
        boolean last
) {
}