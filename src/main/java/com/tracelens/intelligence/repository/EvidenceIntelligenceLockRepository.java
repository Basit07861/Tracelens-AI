package com.tracelens.intelligence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;

import com.tracelens.evidence.entity.Evidence;

import jakarta.persistence.LockModeType;

public interface EvidenceIntelligenceLockRepository
        extends Repository<Evidence, Long> {

    /*
     * Locks the evidence database row until the surrounding
     * transaction finishes.
     *
     * This prevents two intelligence-generation requests
     * from starting for the same evidence simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Evidence>
            findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                    Long id,
                    String ownerEmail
            );
}