package com.tracelens.dashboard.dto;

import java.util.List;

import com.tracelens.investigation.dto.CaseResponse;

public record DashboardResponse(

        long totalCases,

        long openCases,

        long inProgressCases,

        long completedCases,

        long archivedCases,

        long totalEvidence,

        long processedEvidence,

        long highRiskAnalyses,

        List<StatusCount> casesByStatus,

        List<PriorityCount> casesByPriority,

        List<CaseResponse> recentlyUpdatedCases
) {
}