package com.tracelens.investigation.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.tracelens.investigation.entity.InvestigationCase;

public interface InvestigationCaseRepository
        extends JpaRepository<InvestigationCase, Long>,
                JpaSpecificationExecutor<InvestigationCase> {

    boolean existsByCaseNumber(String caseNumber);

    Optional<InvestigationCase> findByIdAndOwnerEmailIgnoreCase(
            Long id,
            String ownerEmail
    );

    Page<InvestigationCase> findAllByOwnerEmailIgnoreCase(
            String ownerEmail,
            Pageable pageable
    );

    long countByOwnerEmailIgnoreCase(String ownerEmail);
}