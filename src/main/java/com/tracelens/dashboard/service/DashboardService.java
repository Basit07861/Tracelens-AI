package com.tracelens.dashboard.service;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.ai.entity.AiPreviewRiskLevel;
import com.tracelens.ai.repository.AiEvidenceAnalysisRepository;
import com.tracelens.dashboard.dto.DashboardResponse;
import com.tracelens.dashboard.dto.PriorityCount;
import com.tracelens.dashboard.dto.StatusCount;
import com.tracelens.evidence.entity.EvidenceStatus;
import com.tracelens.evidence.repository.EvidenceRepository;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.investigation.dto.CaseResponse;
import com.tracelens.investigation.entity.CasePriority;
import com.tracelens.investigation.entity.CaseStatus;
import com.tracelens.investigation.entity.InvestigationCase;
import com.tracelens.investigation.repository.InvestigationCaseRepository;
import com.tracelens.user.entity.User;

@Service
public class DashboardService {

    private static final int RECENT_CASE_LIMIT = 5;

    private final InvestigationCaseRepository
            caseRepository;

    private final EvidenceRepository
            evidenceRepository;

    private final AiEvidenceAnalysisRepository
            analysisRepository;

    public DashboardService(
            InvestigationCaseRepository caseRepository,

            EvidenceRepository evidenceRepository,

            AiEvidenceAnalysisRepository analysisRepository
    ) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.analysisRepository = analysisRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(
            String authenticatedEmail
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        long totalCases =
                caseRepository
                        .countByOwnerEmailIgnoreCase(
                                normalizedEmail
                        );

        long openCases =
                countCasesByStatus(
                        normalizedEmail,
                        CaseStatus.OPEN
                );

        long inProgressCases =
                countCasesByStatus(
                        normalizedEmail,
                        CaseStatus.IN_PROGRESS
                );

        long completedCases =
                countCasesByStatus(
                        normalizedEmail,
                        CaseStatus.COMPLETED
                );

        long archivedCases =
                countCasesByStatus(
                        normalizedEmail,
                        CaseStatus.ARCHIVED
                );

        long totalEvidence =
                evidenceRepository
                        .countByInvestigationCaseOwnerEmailIgnoreCase(
                                normalizedEmail
                        );

        long processedEvidence =
                evidenceRepository
                        .countByInvestigationCaseOwnerEmailIgnoreCaseAndStatus(
                                normalizedEmail,
                                EvidenceStatus.PROCESSED
                        );

        long highRiskAnalyses =
                analysisRepository
                        .countByEvidenceInvestigationCaseOwnerEmailIgnoreCaseAndRiskLevel(
                                normalizedEmail,
                                AiPreviewRiskLevel.HIGH
                        );

        List<StatusCount> casesByStatus =
                List.of(
                        new StatusCount(
                                CaseStatus.OPEN.name(),
                                openCases
                        ),

                        new StatusCount(
                                CaseStatus.IN_PROGRESS.name(),
                                inProgressCases
                        ),

                        new StatusCount(
                                CaseStatus.COMPLETED.name(),
                                completedCases
                        ),

                        new StatusCount(
                                CaseStatus.ARCHIVED.name(),
                                archivedCases
                        )
                );

        List<PriorityCount> casesByPriority =
                List.of(
                        createPriorityCount(
                                normalizedEmail,
                                CasePriority.LOW
                        ),

                        createPriorityCount(
                                normalizedEmail,
                                CasePriority.MEDIUM
                        ),

                        createPriorityCount(
                                normalizedEmail,
                                CasePriority.HIGH
                        ),

                        createPriorityCount(
                                normalizedEmail,
                                CasePriority.CRITICAL
                        )
                );

        List<CaseResponse> recentlyUpdatedCases =
                getRecentlyUpdatedCases(normalizedEmail);

        return new DashboardResponse(
                totalCases,
                openCases,
                inProgressCases,
                completedCases,
                archivedCases,
                totalEvidence,
                processedEvidence,
                highRiskAnalyses,
                casesByStatus,
                casesByPriority,
                recentlyUpdatedCases
        );
    }

    private long countCasesByStatus(
            String normalizedEmail,
            CaseStatus status
    ) {

        return caseRepository
                .countByOwnerEmailIgnoreCaseAndStatus(
                        normalizedEmail,
                        status
                );
    }

    private PriorityCount createPriorityCount(
            String normalizedEmail,
            CasePriority priority
    ) {

        long count =
                caseRepository
                        .countByOwnerEmailIgnoreCaseAndPriority(
                                normalizedEmail,
                                priority
                        );

        return new PriorityCount(
                priority.name(),
                count
        );
    }

    private List<CaseResponse> getRecentlyUpdatedCases(
            String normalizedEmail
    ) {

        PageRequest pageRequest =
                PageRequest.of(
                        0,
                        RECENT_CASE_LIMIT,
                        Sort.by(
                                Sort.Order.desc("updatedAt"),
                                Sort.Order.desc("id")
                        )
                );

        Page<InvestigationCase> recentCasePage =
                caseRepository
                        .findAllByOwnerEmailIgnoreCase(
                                normalizedEmail,
                                pageRequest
                        );

        return recentCasePage
                .getContent()
                .stream()
                .map(this::mapCase)
                .toList();
    }

    private CaseResponse mapCase(
            InvestigationCase investigationCase
    ) {

        User owner = investigationCase.getOwner();

        return new CaseResponse(
                investigationCase.getId(),
                investigationCase.getCaseNumber(),
                investigationCase.getTitle(),
                investigationCase.getDescription(),
                investigationCase.getStatus(),
                investigationCase.getPriority(),
                owner.getId(),
                owner.getFullName(),
                owner.getEmail(),
                investigationCase.getCreatedAt(),
                investigationCase.getUpdatedAt()
        );
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidRequestException(
                    "Authenticated user is unavailable"
            );
        }

        return email
                .strip()
                .toLowerCase(Locale.ROOT);
    }
}