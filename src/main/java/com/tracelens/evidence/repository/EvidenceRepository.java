package com.tracelens.evidence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.evidence.entity.Evidence;
import com.tracelens.evidence.entity.EvidenceStatus;

public interface EvidenceRepository
        extends JpaRepository<Evidence, Long> {

    boolean existsByStoredFileName(
            String storedFileName
    );

    boolean existsByInvestigationCaseIdAndSha256Hash(
            Long caseId,
            String sha256Hash
    );

    Optional<Evidence>
            findByInvestigationCaseIdAndSha256Hash(
                    Long caseId,
                    String sha256Hash
            );

    Optional<Evidence>
            findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                    Long evidenceId,
                    String ownerEmail
            );

    Page<Evidence>
            findAllByInvestigationCaseIdAndInvestigationCaseOwnerEmailIgnoreCase(
                    Long caseId,
                    String ownerEmail,
                    Pageable pageable
            );

    /*
     * Used by the Day 10 final case report.
     * Returns owned evidence in chronological upload order.
     */
    List<Evidence>
            findAllByInvestigationCaseIdAndInvestigationCaseOwnerEmailIgnoreCaseOrderByUploadedAtAsc(
                    Long caseId,
                    String ownerEmail
            );

    long countByInvestigationCaseIdAndInvestigationCaseOwnerEmailIgnoreCase(
            Long caseId,
            String ownerEmail
    );

    /*
     * Day 11 dashboard queries.
     */
    long countByInvestigationCaseOwnerEmailIgnoreCase(
            String ownerEmail
    );

    long countByInvestigationCaseOwnerEmailIgnoreCaseAndStatus(
            String ownerEmail,
            EvidenceStatus status
    );
}