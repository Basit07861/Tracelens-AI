package com.tracelens.intelligence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.intelligence.entity.ExtractedEntity;
import com.tracelens.intelligence.entity.ExtractedEntityType;

public interface ExtractedEntityRepository
        extends JpaRepository<ExtractedEntity, Long> {

    /*
     * Used when constructing a complete run response.
     */
    List<ExtractedEntity>
            findAllByIntelligenceRunIdOrderByIdAsc(
                    Long runId
            );

    /*
     * Alternative ordered retrieval used for a stable,
     * grouped entity response.
     */
    List<ExtractedEntity>
            findAllByIntelligenceRunIdOrderByEntityTypeAscNormalizedValueAsc(
                    Long runId
            );

    /*
     * Retrieves every entity for a run with pagination.
     */
    Page<ExtractedEntity>
            findAllByIntelligenceRunId(
                    Long runId,
                    Pageable pageable
            );

    /*
     * Retrieves only entities of the selected type.
     *
     * Example:
     * ORGANIZATION, MONEY or DATE_TIME.
     */
    Page<ExtractedEntity>
            findAllByIntelligenceRunIdAndEntityType(
                    Long runId,
                    ExtractedEntityType entityType,
                    Pageable pageable
            );

    /*
     * Counts the persisted entities belonging to a run.
     */
    long countByIntelligenceRunId(
            Long runId
    );

    /*
     * Intended only for a failed or incomplete run cleanup.
     * Regeneration will not delete old completed entities.
     */
    void deleteAllByIntelligenceRunId(
            Long runId
    );
}