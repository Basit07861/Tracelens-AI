package com.tracelens.ai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.tracelens.evidence.entity.Evidence;

import jakarta.persistence.LockModeType;

public interface AiEvidenceAnalysisLockRepository
        extends Repository<Evidence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select evidence
            from Evidence evidence
            join fetch evidence.investigationCase investigationCase
            join fetch investigationCase.owner owner
            where evidence.id = :evidenceId
              and lower(owner.email) = lower(:ownerEmail)
            """)
    Optional<Evidence> findOwnedEvidenceForUpdate(
            @Param("evidenceId")
            Long evidenceId,

            @Param("ownerEmail")
            String ownerEmail
    );
}