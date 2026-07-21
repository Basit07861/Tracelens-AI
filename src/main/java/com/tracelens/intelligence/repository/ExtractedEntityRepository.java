package com.tracelens.intelligence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.intelligence.entity.ExtractedEntity;
import com.tracelens.intelligence.entity.ExtractedEntityType;

public interface ExtractedEntityRepository
        extends JpaRepository<ExtractedEntity, Long> {

    List<ExtractedEntity>
            findAllByIntelligenceRunIdOrderByEntityTypeAscNormalizedValueAsc(
                    Long intelligenceRunId
            );

    Page<ExtractedEntity>
            findAllByIntelligenceRunEvidenceIdAndIntelligenceRunEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                    Long evidenceId,
                    String ownerEmail,
                    Pageable pageable
            );

    Page<ExtractedEntity>
            findAllByIntelligenceRunIdAndEntityType(
                    Long intelligenceRunId,
                    ExtractedEntityType entityType,
                    Pageable pageable
            );

    Optional<ExtractedEntity>
            findByIntelligenceRunIdAndEntityTypeAndNormalizedValue(
                    Long intelligenceRunId,
                    ExtractedEntityType entityType,
                    String normalizedValue
            );

    long countByIntelligenceRunId(
            Long intelligenceRunId
    );
}