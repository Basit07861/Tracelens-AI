package com.tracelens.intelligence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.intelligence.entity.TimelineEvent;
import com.tracelens.intelligence.entity.TimelineEventCertainty;
import com.tracelens.intelligence.entity.TimelineTemporalPrecision;

public interface TimelineEventRepository
        extends JpaRepository<TimelineEvent, Long> {

    /*
     * Used when constructing the complete intelligence
     * run response.
     */
    List<TimelineEvent>
            findAllByIntelligenceRunIdOrderBySequenceNumberAsc(
                    Long runId
            );

    /*
     * Retrieves every timeline event for a run.
     *
     * The service will apply sequence-number sorting
     * through Pageable.
     */
    Page<TimelineEvent>
            findAllByIntelligenceRunId(
                    Long runId,
                    Pageable pageable
            );

    /*
     * Filters timeline events by certainty.
     *
     * Supported values:
     * OBSERVED, INFERRED and UNKNOWN.
     */
    Page<TimelineEvent>
            findAllByIntelligenceRunIdAndCertainty(
                    Long runId,
                    TimelineEventCertainty certainty,
                    Pageable pageable
            );

    /*
     * Filters timeline events by temporal precision.
     */
    Page<TimelineEvent>
            findAllByIntelligenceRunIdAndTemporalPrecision(
                    Long runId,
                    TimelineTemporalPrecision temporalPrecision,
                    Pageable pageable
            );

    /*
     * Applies both optional timeline filters together.
     */
    Page<TimelineEvent>
            findAllByIntelligenceRunIdAndCertaintyAndTemporalPrecision(
                    Long runId,
                    TimelineEventCertainty certainty,
                    TimelineTemporalPrecision temporalPrecision,
                    Pageable pageable
            );

    /*
     * Counts the persisted events belonging to a run.
     */
    long countByIntelligenceRunId(
            Long runId
    );

    /*
     * Intended only for failed or incomplete run cleanup.
     * Regeneration will preserve completed historical events.
     */
    void deleteAllByIntelligenceRunId(
            Long runId
    );
}