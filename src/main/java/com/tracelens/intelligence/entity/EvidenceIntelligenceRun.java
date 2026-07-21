package com.tracelens.intelligence.entity;

import java.time.Instant;

import com.tracelens.ai.entity.AiEvidenceAnalysis;
import com.tracelens.evidence.entity.Evidence;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "evidence_intelligence_runs",
        indexes = {
                @Index(
                        name = "idx_intelligence_runs_evidence",
                        columnList = "evidence_id"
                ),
                @Index(
                        name = "idx_intelligence_runs_analysis",
                        columnList = "source_analysis_id"
                ),
                @Index(
                        name = "idx_intelligence_runs_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_intelligence_runs_method",
                        columnList = "method"
                ),
                @Index(
                        name = "idx_intelligence_runs_requested",
                        columnList = "requested_at"
                ),
                @Index(
                        name = "idx_intelligence_runs_completed",
                        columnList = "completed_at"
                )
        }
)
public class EvidenceIntelligenceRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "evidence_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_intelligence_runs_evidence"
            )
    )
    private Evidence evidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "source_analysis_id",
            foreignKey = @ForeignKey(
                    name = "fk_intelligence_runs_analysis"
            )
    )
    private AiEvidenceAnalysis sourceAnalysis;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private IntelligenceRunStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private IntelligenceMethod method;

    @Column(
            name = "human_review_required",
            nullable = false
    )
    private boolean humanReviewRequired = true;

    @Column(length = 100)
    private String provider;

    @Column(length = 150)
    private String model;

    @Column(
            name = "prompt_version",
            length = 50
    )
    private String promptVersion;

    @Column(
            name = "response_schema_version",
            length = 50
    )
    private String responseSchemaVersion;

    @Column(
            name = "source_evidence_sha256",
            nullable = false,
            length = 64
    )
    private String sourceEvidenceSha256;

    @Column(
            name = "source_text_sha256",
            nullable = false,
            length = 64
    )
    private String sourceTextSha256;

    @Column(
            name = "entity_count",
            nullable = false
    )
    private int entityCount;

    @Column(
            name = "timeline_event_count",
            nullable = false
    )
    private int timelineEventCount;

    @Column(
            name = "failure_message",
            length = 1000
    )
    private String failureMessage;

    @Column(
            name = "requested_at",
            nullable = false,
            updatable = false
    )
    private Instant requestedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    public EvidenceIntelligenceRun() {
    }

    @PrePersist
    public void beforeInsert() {

        Instant currentTime = Instant.now();

        if (status == null) {
            status = IntelligenceRunStatus.PENDING;
        }

        if (method == null) {
            method = IntelligenceMethod.HYBRID;
        }

        humanReviewRequired = true;
        entityCount = Math.max(entityCount, 0);

        timelineEventCount =
                Math.max(timelineEventCount, 0);

        requestedAt = currentTime;
        updatedAt = currentTime;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = Instant.now();
        humanReviewRequired = true;
    }

    public void markProcessing() {

        status = IntelligenceRunStatus.PROCESSING;
        startedAt = Instant.now();
        completedAt = null;
        failureMessage = null;
        entityCount = 0;
        timelineEventCount = 0;
    }

    public void markCompleted(
            int entityCount,
            int timelineEventCount
    ) {

        this.entityCount = Math.max(entityCount, 0);

        this.timelineEventCount =
                Math.max(timelineEventCount, 0);

        status = IntelligenceRunStatus.COMPLETED;
        failureMessage = null;
        humanReviewRequired = true;
        completedAt = Instant.now();
    }

    public void markFailed(
            String safeFailureMessage
    ) {

        entityCount = 0;
        timelineEventCount = 0;
        failureMessage = safeFailureMessage;
        humanReviewRequired = true;
        status = IntelligenceRunStatus.FAILED;
        completedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(
            Evidence evidence
    ) {
        this.evidence = evidence;
    }

    public AiEvidenceAnalysis getSourceAnalysis() {
        return sourceAnalysis;
    }

    public void setSourceAnalysis(
            AiEvidenceAnalysis sourceAnalysis
    ) {
        this.sourceAnalysis = sourceAnalysis;
    }

    public IntelligenceRunStatus getStatus() {
        return status;
    }

    public void setStatus(
            IntelligenceRunStatus status
    ) {
        this.status = status;
    }

    public IntelligenceMethod getMethod() {
        return method;
    }

    public void setMethod(
            IntelligenceMethod method
    ) {
        this.method = method;
    }

    public boolean isHumanReviewRequired() {
        return humanReviewRequired;
    }

    public void setHumanReviewRequired(
            boolean humanReviewRequired
    ) {
        this.humanReviewRequired = true;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(
            String provider
    ) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(
            String model
    ) {
        this.model = model;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(
            String promptVersion
    ) {
        this.promptVersion = promptVersion;
    }

    public String getResponseSchemaVersion() {
        return responseSchemaVersion;
    }

    public void setResponseSchemaVersion(
            String responseSchemaVersion
    ) {
        this.responseSchemaVersion =
                responseSchemaVersion;
    }

    public String getSourceEvidenceSha256() {
        return sourceEvidenceSha256;
    }

    public void setSourceEvidenceSha256(
            String sourceEvidenceSha256
    ) {
        this.sourceEvidenceSha256 =
                sourceEvidenceSha256;
    }

    public String getSourceTextSha256() {
        return sourceTextSha256;
    }

    public void setSourceTextSha256(
            String sourceTextSha256
    ) {
        this.sourceTextSha256 =
                sourceTextSha256;
    }

    public int getEntityCount() {
        return entityCount;
    }

    public void setEntityCount(
            int entityCount
    ) {
        this.entityCount = Math.max(
                entityCount,
                0
        );
    }

    public int getTimelineEventCount() {
        return timelineEventCount;
    }

    public void setTimelineEventCount(
            int timelineEventCount
    ) {
        this.timelineEventCount = Math.max(
                timelineEventCount,
                0
        );
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(
            String failureMessage
    ) {
        this.failureMessage = failureMessage;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}