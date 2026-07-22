package com.tracelens.intelligence.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.intelligence.entity.EvidenceIntelligenceRun;
import com.tracelens.intelligence.entity.IntelligenceRunStatus;

public interface EvidenceIntelligenceRunRepository
        extends JpaRepository<EvidenceIntelligenceRun, Long> {

    /*
     * Used when retrieving one run securely.
     *
     * The complete ownership path is included so another
     * investigator cannot retrieve a run by guessing its ID.
     */
    @EntityGraph(
            attributePaths = {
                    "evidence",
                    "evidence.investigationCase",
                    "evidence.investigationCase.owner"
            }
    )
    Optional<EvidenceIntelligenceRun>
            findByIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                    Long runId,
                    String ownerEmail
            );

    /*
     * Retrieves the newest run for owned evidence.
     */
    @EntityGraph(
            attributePaths = {
                    "evidence",
                    "evidence.investigationCase",
                    "evidence.investigationCase.owner"
            }
    )
    Optional<EvidenceIntelligenceRun>
            findFirstByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCaseOrderByRequestedAtDesc(
                    Long evidenceId,
                    String ownerEmail
            );

    /*
     * Retrieves paginated run history.
     *
     * The service will supply requestedAt DESC sorting
     * through the Pageable object.
     */
    @EntityGraph(
            attributePaths = {
                    "evidence",
                    "evidence.investigationCase",
                    "evidence.investigationCase.owner"
            }
    )
    Page<EvidenceIntelligenceRun>
            findAllByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                    Long evidenceId,
                    String ownerEmail,
                    Pageable pageable
            );

    /*
     * Existing generation flow may use this to find the
     * newest run without applying ownership in the query.
     *
     * Ownership must already have been validated before
     * this method is used.
     */
    Optional<EvidenceIntelligenceRun>
            findFirstByEvidenceIdOrderByRequestedAtDesc(
                    Long evidenceId
            );

    /*
     * Retrieves the newest run with a particular status.
     */
    Optional<EvidenceIntelligenceRun>
            findFirstByEvidenceIdAndStatusOrderByRequestedAtDesc(
                    Long evidenceId,
                    IntelligenceRunStatus status
            );

    /*
     * Determines whether evidence has any previous
     * intelligence runs.
     */
    boolean existsByEvidenceId(
            Long evidenceId
    );

    /*
     * Determines whether evidence currently has a run
     * in PENDING or PROCESSING state.
     */
    boolean existsByEvidenceIdAndStatusIn(
            Long evidenceId,
            Collection<IntelligenceRunStatus> statuses
    );

    /*
     * Useful for history and regeneration verification.
     */
    long countByEvidenceId(
            Long evidenceId
    );
}