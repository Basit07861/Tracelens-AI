package com.tracelens.ai.dto;

import java.util.List;

import com.tracelens.ai.entity.AiPreviewRiskLevel;

public record AiEvidencePreviewContent(
        String summary,
        AiPreviewRiskLevel riskLevel,
        List<String> keyIndicators,
        Boolean sufficientInformation,
        List<String> limitations
) {
}