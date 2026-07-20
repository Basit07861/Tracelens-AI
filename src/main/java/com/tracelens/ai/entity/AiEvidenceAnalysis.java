package com.tracelens.ai.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.tracelens.evidence.entity.Evidence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "ai_evidence_analyses",
        indexes = {
                @Index(
                        name = "idx_ai_analyses_evidence",
                        columnList = "evidence_id"
                ),
                @Index(
                        name = "idx_ai_analyses_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_ai_analyses_risk",
                        columnList = "risk_level"
                ),
                @Index(
                        name = "idx_ai_analyses_requested_at",
                        columnList = "requested_at"
                ),
                @Index(
                        name = "idx_ai_analyses_completed_at",
                        columnList = "completed_at"
                )
        }
)
public class AiEvidenceAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "evidence_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_ai_analyses_evidence"
            )
    )
    private Evidence evidence;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private AiAnalysisStatus status;

    /*
     * We intentionally reuse the preliminary risk enum created
     * during Day 7 because the persistent analysis uses the same
     * controlled values:
     *
     * LOW, MEDIUM, HIGH, CRITICAL and UNKNOWN.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "risk_level",
            length = 30
    )
    private AiPreviewRiskLevel riskLevel;

    /*
     * MySQL TEXT is sufficient for the validated summary limit.
     * @Lob is not used because this field stores normal text.
     */
    @Column(
            columnDefinition = "TEXT"
    )
    private String summary;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ai_analysis_findings",
            joinColumns = @JoinColumn(
                    name = "analysis_id",
                    nullable = false,
                    foreignKey = @ForeignKey(
                            name = "fk_ai_findings_analysis"
                    )
            )
    )
    @OrderColumn(name = "finding_order")
    @Column(
            name = "finding_text",
            nullable = false,
            length = 1000
    )
    private List<String> suspiciousFindings =
            new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ai_analysis_actions",
            joinColumns = @JoinColumn(
                    name = "analysis_id",
                    nullable = false,
                    foreignKey = @ForeignKey(
                            name = "fk_ai_actions_analysis"
                    )
            )
    )
    @OrderColumn(name = "action_order")
    @Column(
            name = "action_text",
            nullable = false,
            length = 1000
    )
    private List<String> recommendedActions =
            new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ai_analysis_limitations",
            joinColumns = @JoinColumn(
                    name = "analysis_id",
                    nullable = false,
                    foreignKey = @ForeignKey(
                            name = "fk_ai_limitations_analysis"
                    )
            )
    )
    @OrderColumn(name = "limitation_order")
    @Column(
            name = "limitation_text",
            nullable = false,
            length = 1000
    )
    private List<String> limitations =
            new ArrayList<>();

    @Column(name = "sufficient_information")
    private Boolean sufficientInformation;

    /*
     * This value is controlled by the application and must never
     * be decided by the AI model.
     */
    @Column(
            name = "human_review_required",
            nullable = false
    )
    private boolean humanReviewRequired = true;

    @Column(
            nullable = false,
            length = 100
    )
    private String provider;

    @Column(
            nullable = false,
            length = 150
    )
    private String model;

    @Column(
            name = "prompt_version",
            nullable = false,
            length = 50
    )
    private String promptVersion;

    @Column(
            name = "response_schema_version",
            nullable = false,
            length = 50
    )
    private String responseSchemaVersion;

    /*
     * SHA-256 value of the original physical evidence bytes at
     * the time the analysis was requested.
     */
    @Column(
            name = "source_evidence_sha256",
            nullable = false,
            length = 64
    )
    private String sourceEvidenceSha256;

    /*
     * SHA-256 of the exact extracted text supplied for analysis.
     * It lets TraceLens determine whether a later extraction
     * differs from the text used for this analysis.
     */
    @Column(
            name = "source_text_sha256",
            nullable = false,
            length = 64
    )
    private String sourceTextSha256;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    /*
     * Only safe application-level failure information is stored.
     * Provider response bodies, keys and stack traces must never
     * be persisted here.
     */
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

    public AiEvidenceAnalysis() {
    }

    @PrePersist
    public void beforeInsert() {

        Instant currentTime = Instant.now();

        if (status == null) {
            status = AiAnalysisStatus.PENDING;
        }

        humanReviewRequired = true;
        requestedAt = currentTime;
        updatedAt = currentTime;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = Instant.now();
    }

    public void markProcessing() {

        status = AiAnalysisStatus.PROCESSING;
        startedAt = Instant.now();
        completedAt = null;
        failureMessage = null;

        clearGeneratedContent();
    }

    public void markCompleted(
            String summary,
            AiPreviewRiskLevel riskLevel,
            List<String> suspiciousFindings,
            List<String> recommendedActions,
            Boolean sufficientInformation,
            List<String> limitations,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens
    ) {

        this.summary = summary;
        this.riskLevel = riskLevel;

        replaceValues(
                this.suspiciousFindings,
                suspiciousFindings
        );

        replaceValues(
                this.recommendedActions,
                recommendedActions
        );

        replaceValues(
                this.limitations,
                limitations
        );

        this.sufficientInformation =
                sufficientInformation;

        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;

        this.failureMessage = null;
        this.humanReviewRequired = true;
        this.completedAt = Instant.now();
        this.status = AiAnalysisStatus.COMPLETED;
    }

    public void markFailed(
            String safeFailureMessage
    ) {

        clearGeneratedContent();

        failureMessage = safeFailureMessage;
        humanReviewRequired = true;
        completedAt = Instant.now();
        status = AiAnalysisStatus.FAILED;
    }

    private void clearGeneratedContent() {

        summary = null;
        riskLevel = null;
        sufficientInformation = null;

        suspiciousFindings.clear();
        recommendedActions.clear();
        limitations.clear();

        promptTokens = null;
        completionTokens = null;
        totalTokens = null;
    }

    private void replaceValues(
            List<String> destination,
            List<String> source
    ) {

        destination.clear();

        if (source != null) {
            destination.addAll(source);
        }
    }

    public Long getId() {
        return id;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(
            Evidence evidence
    ) {
        this.evidence = evidence;
    }

    public AiAnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(
            AiAnalysisStatus status
    ) {
        this.status = status;
    }

    public AiPreviewRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(
            AiPreviewRiskLevel riskLevel
    ) {
        this.riskLevel = riskLevel;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(
            String summary
    ) {
        this.summary = summary;
    }

    public List<String> getSuspiciousFindings() {
        return suspiciousFindings;
    }

    public void setSuspiciousFindings(
            List<String> suspiciousFindings
    ) {
        replaceValues(
                this.suspiciousFindings,
                suspiciousFindings
        );
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(
            List<String> recommendedActions
    ) {
        replaceValues(
                this.recommendedActions,
                recommendedActions
        );
    }

    public List<String> getLimitations() {
        return limitations;
    }

    public void setLimitations(
            List<String> limitations
    ) {
        replaceValues(
                this.limitations,
                limitations
        );
    }

    public Boolean getSufficientInformation() {
        return sufficientInformation;
    }

    public void setSufficientInformation(
            Boolean sufficientInformation
    ) {
        this.sufficientInformation =
                sufficientInformation;
    }

    public boolean isHumanReviewRequired() {
        return humanReviewRequired;
    }

    public void setHumanReviewRequired(
            boolean humanReviewRequired
    ) {
        /*
         * Human review is mandatory for every analysis.
         */
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
        this.sourceTextSha256 = sourceTextSha256;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(
            Integer promptTokens
    ) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(
            Integer completionTokens
    ) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(
            Integer totalTokens
    ) {
        this.totalTokens = totalTokens;
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