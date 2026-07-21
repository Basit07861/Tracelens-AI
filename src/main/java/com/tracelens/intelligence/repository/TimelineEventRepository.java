package com.tracelens.intelligence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.intelligence.entity.TimelineEvent;
import com.tracelens.intelligence.entity.TimelineEventCertainty;

public interface TimelineEventRepository
        extends JpaRepository<TimelineEvent, Long> {

    List<TimelineEvent>
            findAllByIntelligenceRunIdOrderBySequenceNumberAsc(
                    Long intelligenceRunId
            );

    Page<TimelineEvent>
            findAllByIntelligenceRunEvidenceIdAndIntelligenceRunEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                    Long evidenceId,
                    String ownerEmail,
                    Pageable pageable
            );

    Page<TimelineEvent>
            findAllByIntelligenceRunIdAndCertainty(
                    Long intelligenceRunId,
                    TimelineEventCertainty certainty,
                    Pageable pageable
            );

    long countByIntelligenceRunId(
            Long intelligenceRunId
    );
}