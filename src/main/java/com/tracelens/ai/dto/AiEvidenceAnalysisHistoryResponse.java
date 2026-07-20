package com.tracelens.ai.dto;

import java.util.List;

public record AiEvidenceAnalysisHistoryResponse(
        Long evidenceId,
        List<AiEvidenceAnalysisResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        int numberOfElements,
        boolean first,
        boolean last
) {
}