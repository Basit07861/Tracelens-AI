package com.tracelens.evidence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.evidence.entity.Evidence;

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

    long countByInvestigationCaseIdAndInvestigationCaseOwnerEmailIgnoreCase(
            Long caseId,
            String ownerEmail
    );
}