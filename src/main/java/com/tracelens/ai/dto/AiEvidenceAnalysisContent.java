package com.tracelens.ai.dto;

import java.util.List;

import com.tracelens.ai.entity.AiPreviewRiskLevel;

public record AiEvidenceAnalysisContent(
        String summary,
        AiPreviewRiskLevel riskLevel,
        List<String> suspiciousFindings,
        List<String> recommendedActions,
        Boolean sufficientInformation,
        List<String> limitations
) {
}