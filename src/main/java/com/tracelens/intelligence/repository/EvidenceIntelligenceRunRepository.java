package com.tracelens.intelligence.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.intelligence.entity.EvidenceIntelligenceRun;
import com.tracelens.intelligence.entity.IntelligenceRunStatus;

public interface EvidenceIntelligenceRunRepository
        extends JpaRepository<
                EvidenceIntelligenceRun,
                Long
        > {

    Optional<EvidenceIntelligenceRun>
            findByIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                    Long runId,
                    String ownerEmail
            );

    Page<EvidenceIntelligenceRun>
            findAllByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                    Long evidenceId,
                    String ownerEmail,
                    Pageable pageable
            );

    Optional<EvidenceIntelligenceRun>
            findFirstByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCaseOrderByRequestedAtDesc(
                    Long evidenceId,
                    String ownerEmail
            );

    boolean existsByEvidenceIdAndStatusIn(
            Long evidenceId,
            Collection<IntelligenceRunStatus> statuses
    );

    long countByEvidenceId(
            Long evidenceId
    );
}