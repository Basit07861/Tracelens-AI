package com.tracelens.ai.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.ai.entity.AiAnalysisStatus;
import com.tracelens.ai.entity.AiEvidenceAnalysis;
import com.tracelens.ai.entity.AiPreviewRiskLevel;

public interface AiEvidenceAnalysisRepository
        extends JpaRepository<AiEvidenceAnalysis, Long> {

    Optional<AiEvidenceAnalysis>
            findByIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                    Long analysisId,
                    String ownerEmail
            );

    Page<AiEvidenceAnalysis>
            findAllByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                    Long evidenceId,
                    String ownerEmail,
                    Pageable pageable
            );

    Optional<AiEvidenceAnalysis>
            findFirstByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCaseOrderByRequestedAtDesc(
                    Long evidenceId,
                    String ownerEmail
            );

    Optional<AiEvidenceAnalysis>
            findFirstByEvidenceIdAndStatusOrderByRequestedAtDesc(
                    Long evidenceId,
                    AiAnalysisStatus status
            );

    boolean existsByEvidenceIdAndStatusIn(
            Long evidenceId,
            Collection<AiAnalysisStatus> statuses
    );

    long countByEvidenceId(
            Long evidenceId
    );

    long countByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
            Long evidenceId,
            String ownerEmail
    );

    long countByEvidenceInvestigationCaseOwnerEmailIgnoreCaseAndRiskLevel(
            String ownerEmail,
            AiPreviewRiskLevel riskLevel
    );
}