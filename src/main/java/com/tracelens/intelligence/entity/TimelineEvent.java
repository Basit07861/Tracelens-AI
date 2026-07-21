package com.tracelens.intelligence.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "timeline_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_timeline_run_sequence",
                        columnNames = {
                                "intelligence_run_id",
                                "sequence_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_timeline_run",
                        columnList = "intelligence_run_id"
                ),
                @Index(
                        name = "idx_timeline_normalized_time",
                        columnList = "normalized_date_time"
                ),
                @Index(
                        name = "idx_timeline_precision",
                        columnList = "temporal_precision"
                ),
                @Index(
                        name = "idx_timeline_certainty",
                        columnList = "certainty"
                )
        }
)
public class TimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "intelligence_run_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_timeline_intelligence_run"
            )
    )
    private EvidenceIntelligenceRun intelligenceRun;

    @Column(
            name = "sequence_number",
            nullable = false
    )
    private int sequenceNumber;

    @Column(
            nullable = false,
            length = 300
    )
    private String title;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "temporal_expression",
            length = 500
    )
    private String temporalExpression;

    @Column(name = "normalized_date_time")
    private LocalDateTime normalizedDateTime;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "temporal_precision",
            nullable = false,
            length = 30
    )
    private TimelineTemporalPrecision temporalPrecision;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private TimelineEventCertainty certainty;

    @Column(
            name = "context_snippet",
            length = 1000
    )
    private String contextSnippet;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "timeline_event_entities",
            joinColumns = @JoinColumn(
                    name = "timeline_event_id",
                    nullable = false,
                    foreignKey = @ForeignKey(
                            name = "fk_timeline_links_event"
                    )
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "entity_id",
                    nullable = false,
                    foreignKey = @ForeignKey(
                            name = "fk_timeline_links_entity"
                    )
            ),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_timeline_event_entity",
                            columnNames = {
                                    "timeline_event_id",
                                    "entity_id"
                            }
                    )
            }
    )
    private Set<ExtractedEntity> involvedEntities =
            new LinkedHashSet<>();

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    public TimelineEvent() {
    }

    @PrePersist
    public void beforeInsert() {

        createdAt = Instant.now();

        if (temporalPrecision == null) {
            temporalPrecision =
                    TimelineTemporalPrecision.UNKNOWN;
        }

        if (certainty == null) {
            certainty =
                    TimelineEventCertainty.UNKNOWN;
        }
    }

    public Long getId() {
        return id;
    }

    public EvidenceIntelligenceRun
            getIntelligenceRun() {

        return intelligenceRun;
    }

    public void setIntelligenceRun(
            EvidenceIntelligenceRun intelligenceRun
    ) {
        this.intelligenceRun = intelligenceRun;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(
            int sequenceNumber
    ) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public String getTemporalExpression() {
        return temporalExpression;
    }

    public void setTemporalExpression(
            String temporalExpression
    ) {
        this.temporalExpression =
                temporalExpression;
    }

    public LocalDateTime getNormalizedDateTime() {
        return normalizedDateTime;
    }

    public void setNormalizedDateTime(
            LocalDateTime normalizedDateTime
    ) {
        this.normalizedDateTime =
                normalizedDateTime;
    }

    public TimelineTemporalPrecision
            getTemporalPrecision() {

        return temporalPrecision;
    }

    public void setTemporalPrecision(
            TimelineTemporalPrecision
                    temporalPrecision
    ) {
        this.temporalPrecision =
                temporalPrecision;
    }

    public TimelineEventCertainty getCertainty() {
        return certainty;
    }

    public void setCertainty(
            TimelineEventCertainty certainty
    ) {
        this.certainty = certainty;
    }

    public String getContextSnippet() {
        return contextSnippet;
    }

    public void setContextSnippet(
            String contextSnippet
    ) {
        this.contextSnippet = contextSnippet;
    }

    public Set<ExtractedEntity>
            getInvolvedEntities() {

        return involvedEntities;
    }

    public void setInvolvedEntities(
            Set<ExtractedEntity> involvedEntities
    ) {

        this.involvedEntities.clear();

        if (involvedEntities != null) {
            this.involvedEntities.addAll(
                    involvedEntities
            );
        }
    }

    public void addInvolvedEntity(
            ExtractedEntity entity
    ) {

        if (entity != null) {
            involvedEntities.add(entity);
        }
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}