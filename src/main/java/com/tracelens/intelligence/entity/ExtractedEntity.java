package com.tracelens.intelligence.entity;

import java.math.BigDecimal;
import java.time.Instant;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "extracted_entities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_entities_run_type_value",
                        columnNames = {
                                "intelligence_run_id",
                                "entity_type",
                                "normalized_value"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_entities_run",
                        columnList = "intelligence_run_id"
                ),
                @Index(
                        name = "idx_entities_type",
                        columnList = "entity_type"
                ),
                @Index(
                        name = "idx_entities_normalized",
                        columnList = "normalized_value"
                ),
                @Index(
                        name = "idx_entities_confidence",
                        columnList = "confidence"
                )
        }
)
public class ExtractedEntity {

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
                    name = "fk_entities_intelligence_run"
            )
    )
    private EvidenceIntelligenceRun intelligenceRun;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "entity_type",
            nullable = false,
            length = 40
    )
    private ExtractedEntityType entityType;

    @Column(
            name = "display_value",
            nullable = false,
            length = 500
    )
    private String displayValue;

    @Column(
            name = "normalized_value",
            nullable = false,
            length = 500
    )
    private String normalizedValue;

    @Column(
            name = "context_snippet",
            length = 1000
    )
    private String contextSnippet;

    @Column(
            precision = 5,
            scale = 4
    )
    private BigDecimal confidence;

    @Column(
            name = "occurrence_count",
            nullable = false
    )
    private int occurrenceCount = 1;

    @Column(name = "first_character_offset")
    private Integer firstCharacterOffset;

    @Column(name = "last_character_offset")
    private Integer lastCharacterOffset;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    public ExtractedEntity() {
    }

    @PrePersist
    public void beforeInsert() {

        createdAt = Instant.now();

        if (occurrenceCount <= 0) {
            occurrenceCount = 1;
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

    public ExtractedEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(
            ExtractedEntityType entityType
    ) {
        this.entityType = entityType;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(
            String displayValue
    ) {
        this.displayValue = displayValue;
    }

    public String getNormalizedValue() {
        return normalizedValue;
    }

    public void setNormalizedValue(
            String normalizedValue
    ) {
        this.normalizedValue = normalizedValue;
    }

    public String getContextSnippet() {
        return contextSnippet;
    }

    public void setContextSnippet(
            String contextSnippet
    ) {
        this.contextSnippet = contextSnippet;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(
            BigDecimal confidence
    ) {
        this.confidence = confidence;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(
            int occurrenceCount
    ) {
        this.occurrenceCount =
                Math.max(occurrenceCount, 1);
    }

    public Integer getFirstCharacterOffset() {
        return firstCharacterOffset;
    }

    public void setFirstCharacterOffset(
            Integer firstCharacterOffset
    ) {
        this.firstCharacterOffset =
                firstCharacterOffset;
    }

    public Integer getLastCharacterOffset() {
        return lastCharacterOffset;
    }

    public void setLastCharacterOffset(
            Integer lastCharacterOffset
    ) {
        this.lastCharacterOffset =
                lastCharacterOffset;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}