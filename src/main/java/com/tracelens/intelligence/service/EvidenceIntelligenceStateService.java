package com.tracelens.intelligence.service;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.ai.entity.AiAnalysisStatus;
import com.tracelens.ai.entity.AiEvidenceAnalysis;
import com.tracelens.ai.repository.AiEvidenceAnalysisRepository;
import com.tracelens.evidence.entity.Evidence;
import com.tracelens.evidence.repository.EvidenceRepository;
import com.tracelens.exception.EvidenceNotFoundException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.intelligence.dto.EvidenceIntelligenceRunHistoryResponse;
import com.tracelens.intelligence.dto.EvidenceIntelligenceRunResponse;
import com.tracelens.intelligence.dto.EvidenceIntelligenceRunSummaryResponse;
import com.tracelens.intelligence.dto.ExtractedEntityPageResponse;
import com.tracelens.intelligence.dto.ExtractedEntityResponse;
import com.tracelens.intelligence.dto.IntelligenceEntityReferenceContent;
import com.tracelens.intelligence.dto.TimelineEventPageResponse;
import com.tracelens.intelligence.dto.TimelineEventResponse;
import com.tracelens.intelligence.entity.EvidenceIntelligenceRun;
import com.tracelens.intelligence.entity.ExtractedEntity;
import com.tracelens.intelligence.entity.ExtractedEntityType;
import com.tracelens.intelligence.entity.IntelligenceMethod;
import com.tracelens.intelligence.entity.IntelligenceRunStatus;
import com.tracelens.intelligence.entity.TimelineEvent;
import com.tracelens.intelligence.entity.TimelineEventCertainty;
import com.tracelens.intelligence.entity.TimelineTemporalPrecision;
import com.tracelens.intelligence.repository.EvidenceIntelligenceRunRepository;
import com.tracelens.intelligence.repository.ExtractedEntityRepository;
import com.tracelens.intelligence.repository.TimelineEventRepository;
import com.tracelens.investigation.entity.InvestigationCase;

@Service
public class EvidenceIntelligenceStateService {

    private static final int MAXIMUM_PAGE_SIZE = 100;

    private static final Set<IntelligenceRunStatus>
            ACTIVE_STATUSES = EnumSet.of(
                    IntelligenceRunStatus.PENDING,
                    IntelligenceRunStatus.PROCESSING
            );

    private final EvidenceRepository evidenceRepository;

    private final AiEvidenceAnalysisRepository
            analysisRepository;

    private final EvidenceIntelligenceRunRepository
            runRepository;

    private final ExtractedEntityRepository
            entityRepository;

    private final TimelineEventRepository
            timelineRepository;

    private final IntelligenceEntityNormalizationService
            normalizationService;

    public EvidenceIntelligenceStateService(
            EvidenceRepository evidenceRepository,

            AiEvidenceAnalysisRepository
                    analysisRepository,

            EvidenceIntelligenceRunRepository
                    runRepository,

            ExtractedEntityRepository entityRepository,

            TimelineEventRepository timelineRepository,

            IntelligenceEntityNormalizationService
                    normalizationService
    ) {
        this.evidenceRepository = evidenceRepository;
        this.analysisRepository = analysisRepository;
        this.runRepository = runRepository;
        this.entityRepository = entityRepository;
        this.timelineRepository = timelineRepository;
        this.normalizationService =
                normalizationService;
    }

    /*
     * Returns the owned evidence information required
     * by the intelligence-generation service.
     */
    @Transactional(readOnly = true)
    public EvidenceIntelligenceTarget getTarget(
            Long evidenceId,
            String authenticatedEmail
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        InvestigationCase investigationCase =
                evidence.getInvestigationCase();

        return new EvidenceIntelligenceTarget(
                evidence.getId(),
                investigationCase.getId(),
                investigationCase.getCaseNumber(),
                evidence.getOriginalFileName(),
                evidence.getFileType(),
                evidence.getStatus(),
                evidence.getSha256Hash(),
                evidence.getExtractedText()
        );
    }

    /*
     * Retrieves one complete intelligence run securely.
     *
     * Ownership is checked using:
     * run -> evidence -> case -> owner -> email.
     */
    @Transactional(readOnly = true)
    public EvidenceIntelligenceRunResponse getRun(
            Long runId,
            String authenticatedEmail
    ) {

        EvidenceIntelligenceRun run =
                findOwnedRun(
                        runId,
                        authenticatedEmail
                );

        List<ExtractedEntity> entities =
                entityRepository
                        .findAllByIntelligenceRunIdOrderByEntityTypeAscNormalizedValueAsc(
                                run.getId()
                        );

        List<TimelineEvent> timelineEvents =
                timelineRepository
                        .findAllByIntelligenceRunIdOrderBySequenceNumberAsc(
                                run.getId()
                        );

        return mapToResponse(
                run,
                entities,
                timelineEvents
        );
    }

    /*
     * Retrieves the newest intelligence run belonging
     * to owned evidence.
     */
    @Transactional(readOnly = true)
    public EvidenceIntelligenceRunResponse getLatestRun(
            Long evidenceId,
            String authenticatedEmail
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        EvidenceIntelligenceRun run =
                runRepository
                        .findFirstByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCaseOrderByRequestedAtDesc(
                                evidence.getId(),
                                normalizedEmail
                        )
                        .orElseThrow(
                                () -> new EvidenceNotFoundException(
                                        "Intelligence run was not found"
                                )
                        );

        List<ExtractedEntity> entities =
                entityRepository
                        .findAllByIntelligenceRunIdOrderByEntityTypeAscNormalizedValueAsc(
                                run.getId()
                        );

        List<TimelineEvent> timelineEvents =
                timelineRepository
                        .findAllByIntelligenceRunIdOrderBySequenceNumberAsc(
                                run.getId()
                        );

        return mapToResponse(
                run,
                entities,
                timelineEvents
        );
    }

    /*
     * Retrieves paginated run history without loading
     * every entity and timeline event for every run.
     */
    @Transactional(readOnly = true)
    public EvidenceIntelligenceRunHistoryResponse
            getRunHistory(

                    Long evidenceId,
                    String authenticatedEmail,
                    int page,
                    int size
            ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        PageRequest pageable =
                createPageRequest(
                        page,
                        size,
                        Sort.by(
                                Sort.Order.desc(
                                        "requestedAt"
                                ),
                                Sort.Order.desc("id")
                        )
                );

        Page<EvidenceIntelligenceRun> runPage =
                runRepository
                        .findAllByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                                evidence.getId(),
                                normalizedEmail,
                                pageable
                        );

        List<EvidenceIntelligenceRunSummaryResponse>
                content =
                        runPage.getContent()
                                .stream()
                                .map(this::mapToSummary)
                                .toList();

        return new EvidenceIntelligenceRunHistoryResponse(
                evidence.getId(),
                content,
                runPage.getNumber(),
                runPage.getSize(),
                runPage.getTotalElements(),
                runPage.getTotalPages(),
                runPage.getNumberOfElements(),
                runPage.isFirst(),
                runPage.isLast()
        );
    }

    /*
     * Retrieves paginated entities for a run.
     *
     * entityType is optional. When it is null,
     * every entity type is returned.
     */
    @Transactional(readOnly = true)
    public ExtractedEntityPageResponse getEntities(
            Long runId,
            String authenticatedEmail,
            ExtractedEntityType entityType,
            int page,
            int size
    ) {

        EvidenceIntelligenceRun run =
                findOwnedRun(
                        runId,
                        authenticatedEmail
                );

        PageRequest pageable =
                createPageRequest(
                        page,
                        size,
                        Sort.by(
                                Sort.Order.asc("entityType"),
                                Sort.Order.asc(
                                        "normalizedValue"
                                ),
                                Sort.Order.asc("id")
                        )
                );

        Page<ExtractedEntity> entityPage;

        if (entityType == null) {

            entityPage =
                    entityRepository
                            .findAllByIntelligenceRunId(
                                    run.getId(),
                                    pageable
                            );
        }
        else {

            entityPage =
                    entityRepository
                            .findAllByIntelligenceRunIdAndEntityType(
                                    run.getId(),
                                    entityType,
                                    pageable
                            );
        }

        List<ExtractedEntityResponse> content =
                entityPage.getContent()
                        .stream()
                        .map(this::mapEntity)
                        .toList();

        return new ExtractedEntityPageResponse(
                run.getId(),
                run.getEvidence().getId(),
                entityType,
                content,
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.getNumberOfElements(),
                entityPage.isFirst(),
                entityPage.isLast()
        );
    }

    /*
     * Retrieves paginated timeline events.
     *
     * certainty and temporalPrecision are optional.
     * The correct repository query is selected based
     * on the supplied filters.
     */
    @Transactional(readOnly = true)
    public TimelineEventPageResponse getTimelineEvents(
            Long runId,
            String authenticatedEmail,
            TimelineEventCertainty certainty,
            TimelineTemporalPrecision temporalPrecision,
            int page,
            int size
    ) {

        EvidenceIntelligenceRun run =
                findOwnedRun(
                        runId,
                        authenticatedEmail
                );

        PageRequest pageable =
                createPageRequest(
                        page,
                        size,
                        Sort.by(
                                Sort.Order.asc(
                                        "sequenceNumber"
                                ),
                                Sort.Order.asc("id")
                        )
                );

        Page<TimelineEvent> timelinePage;

        if (
                certainty != null
                && temporalPrecision != null
        ) {

            timelinePage =
                    timelineRepository
                            .findAllByIntelligenceRunIdAndCertaintyAndTemporalPrecision(
                                    run.getId(),
                                    certainty,
                                    temporalPrecision,
                                    pageable
                            );
        }
        else if (certainty != null) {

            timelinePage =
                    timelineRepository
                            .findAllByIntelligenceRunIdAndCertainty(
                                    run.getId(),
                                    certainty,
                                    pageable
                            );
        }
        else if (temporalPrecision != null) {

            timelinePage =
                    timelineRepository
                            .findAllByIntelligenceRunIdAndTemporalPrecision(
                                    run.getId(),
                                    temporalPrecision,
                                    pageable
                            );
        }
        else {

            timelinePage =
                    timelineRepository
                            .findAllByIntelligenceRunId(
                                    run.getId(),
                                    pageable
                            );
        }

        List<TimelineEventResponse> content =
                timelinePage.getContent()
                        .stream()
                        .map(this::mapEvent)
                        .toList();

        return new TimelineEventPageResponse(
                run.getId(),
                run.getEvidence().getId(),
                certainty,
                temporalPrecision,
                content,
                timelinePage.getNumber(),
                timelinePage.getSize(),
                timelinePage.getTotalElements(),
                timelinePage.getTotalPages(),
                timelinePage.getNumberOfElements(),
                timelinePage.isFirst(),
                timelinePage.isLast()
        );
    }

    /*
     * Creates the pending database row before the
     * longer intelligence extraction process begins.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public Long createPendingRun(
            Long evidenceId,
            String authenticatedEmail,
            String provider,
            String model,
            String promptVersion,
            String responseSchemaVersion,
            String sourceEvidenceSha256,
            String sourceTextSha256
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        boolean activeRunExists =
                runRepository
                        .existsByEvidenceIdAndStatusIn(
                                evidenceId,
                                ACTIVE_STATUSES
                        );

        if (activeRunExists) {
            throw new InvalidRequestException(
                    "An intelligence extraction run is "
                    + "already pending or processing "
                    + "for this evidence"
            );
        }

        AiEvidenceAnalysis sourceAnalysis =
                analysisRepository
                        .findFirstByEvidenceIdAndStatusOrderByRequestedAtDesc(
                                evidenceId,
                                AiAnalysisStatus.COMPLETED
                        )
                        .orElse(null);

        EvidenceIntelligenceRun run =
                new EvidenceIntelligenceRun();

        run.setEvidence(evidence);
        run.setSourceAnalysis(sourceAnalysis);
        run.setStatus(IntelligenceRunStatus.PENDING);
        run.setMethod(IntelligenceMethod.HYBRID);
        run.setProvider(provider);
        run.setModel(model);
        run.setPromptVersion(promptVersion);

        run.setResponseSchemaVersion(
                responseSchemaVersion
        );

        run.setSourceEvidenceSha256(
                sourceEvidenceSha256
        );

        run.setSourceTextSha256(
                sourceTextSha256
        );

        EvidenceIntelligenceRun savedRun =
                runRepository.saveAndFlush(run);

        return savedRun.getId();
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markProcessing(
            Long runId
    ) {

        EvidenceIntelligenceRun run =
                findRun(runId);

        run.markProcessing();

        runRepository.saveAndFlush(run);
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public EvidenceIntelligenceRunResponse markCompleted(
            Long runId,
            List<IntelligenceEntityCandidate>
                    entityCandidates,
            List<IntelligenceTimelineCandidate>
                    timelineCandidates
    ) {

        EvidenceIntelligenceRun run =
                findRun(runId);

        List<ExtractedEntity> savedEntities =
                saveEntities(
                        run,
                        entityCandidates
                );

        List<TimelineEvent> savedEvents =
                saveTimelineEvents(
                        run,
                        timelineCandidates,
                        savedEntities
                );

        run.markCompleted(
                savedEntities.size(),
                savedEvents.size()
        );

        EvidenceIntelligenceRun savedRun =
                runRepository.saveAndFlush(run);

        return mapToResponse(
                savedRun,
                savedEntities,
                savedEvents
        );
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markFailed(
            Long runId,
            String safeFailureMessage
    ) {

        EvidenceIntelligenceRun run =
                findRun(runId);

        run.markFailed(safeFailureMessage);

        runRepository.saveAndFlush(run);
    }

    private List<ExtractedEntity> saveEntities(
            EvidenceIntelligenceRun run,
            List<IntelligenceEntityCandidate>
                    candidates
    ) {

        List<ExtractedEntity> entities =
                candidates.stream()
                        .map(candidate -> {

                            ExtractedEntity entity =
                                    new ExtractedEntity();

                            entity.setIntelligenceRun(run);

                            entity.setEntityType(
                                    candidate.entityType()
                            );

                            entity.setDisplayValue(
                                    candidate.displayValue()
                            );

                            entity.setNormalizedValue(
                                    candidate.normalizedValue()
                            );

                            entity.setContextSnippet(
                                    candidate.contextSnippet()
                            );

                            entity.setConfidence(
                                    candidate.confidence()
                            );

                            entity.setOccurrenceCount(
                                    candidate.occurrenceCount()
                            );

                            entity.setFirstCharacterOffset(
                                    candidate
                                            .firstCharacterOffset()
                            );

                            entity.setLastCharacterOffset(
                                    candidate
                                            .lastCharacterOffset()
                            );

                            return entity;
                        })
                        .toList();

        return entityRepository.saveAllAndFlush(
                entities
        );
    }

    private List<TimelineEvent> saveTimelineEvents(
            EvidenceIntelligenceRun run,
            List<IntelligenceTimelineCandidate>
                    candidates,
            List<ExtractedEntity> savedEntities
    ) {

        Map<String, ExtractedEntity> entitiesByKey =
                new LinkedHashMap<>();

        for (ExtractedEntity entity : savedEntities) {

            String key =
                    normalizationService.createKey(
                            entity.getEntityType(),
                            entity.getNormalizedValue()
                    );

            entitiesByKey.put(key, entity);
        }

        List<TimelineEvent> events =
                candidates.stream()
                        .map(candidate -> {

                            TimelineEvent event =
                                    new TimelineEvent();

                            event.setIntelligenceRun(run);

                            event.setSequenceNumber(
                                    candidate.sequenceNumber()
                            );

                            event.setTitle(
                                    candidate.title()
                            );

                            event.setDescription(
                                    candidate.description()
                            );

                            event.setTemporalExpression(
                                    candidate
                                            .temporalExpression()
                            );

                            event.setNormalizedDateTime(
                                    candidate
                                            .normalizedDateTime()
                            );

                            event.setTemporalPrecision(
                                    candidate
                                            .temporalPrecision()
                            );

                            event.setCertainty(
                                    candidate.certainty()
                            );

                            event.setContextSnippet(
                                    candidate.contextSnippet()
                            );

                            linkEntities(
                                    event,
                                    candidate
                                            .involvedEntities(),
                                    entitiesByKey
                            );

                            return event;
                        })
                        .toList();

        return timelineRepository.saveAllAndFlush(
                events
        );
    }

    private void linkEntities(
            TimelineEvent event,
            List<IntelligenceEntityReferenceContent>
                    references,
            Map<String, ExtractedEntity> entitiesByKey
    ) {

        for (
                IntelligenceEntityReferenceContent reference
                : references
        ) {

            String normalizedValue =
                    normalizationService.normalizeValue(
                            reference.entityType(),
                            reference.value()
                    );

            String key =
                    normalizationService.createKey(
                            reference.entityType(),
                            normalizedValue
                    );

            ExtractedEntity entity =
                    entitiesByKey.get(key);

            if (entity != null) {
                event.addInvolvedEntity(entity);
            }
        }
    }

    private Evidence findOwnedEvidence(
            Long evidenceId,
            String authenticatedEmail
    ) {

        if (evidenceId == null || evidenceId <= 0) {
            throw new EvidenceNotFoundException(
                    "Evidence file was not found"
            );
        }

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        return evidenceRepository
                .findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                        evidenceId,
                        normalizedEmail
                )
                .orElseThrow(
                        () -> new EvidenceNotFoundException(
                                "Evidence file was not found"
                        )
                );
    }

    /*
     * Internal lifecycle lookup.
     *
     * It is used only after a run has already been
     * securely created by the authenticated workflow.
     */
    private EvidenceIntelligenceRun findRun(
            Long runId
    ) {

        if (runId == null || runId <= 0) {
            throw new IllegalStateException(
                    "Intelligence run was not found"
            );
        }

        return runRepository
                .findById(runId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Intelligence run was not found"
                        )
                );
    }

    /*
     * Secure public lookup for retrieval endpoints.
     *
     * An unowned ID produces the same 404 response as
     * a missing ID.
     */
    private EvidenceIntelligenceRun findOwnedRun(
            Long runId,
            String authenticatedEmail
    ) {

        if (runId == null || runId <= 0) {
            throw new EvidenceNotFoundException(
                    "Intelligence run was not found"
            );
        }

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        return runRepository
                .findByIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                        runId,
                        normalizedEmail
                )
                .orElseThrow(
                        () -> new EvidenceNotFoundException(
                                "Intelligence run was not found"
                        )
                );
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidRequestException(
                    "Authenticated user is unavailable"
            );
        }

        return email
                .strip()
                .toLowerCase(Locale.ROOT);
    }

    private PageRequest createPageRequest(
            int page,
            int size,
            Sort sort
    ) {

        if (page < 0) {
            throw new InvalidRequestException(
                    "Page number cannot be negative"
            );
        }

        if (
                size < 1
                || size > MAXIMUM_PAGE_SIZE
        ) {
            throw new InvalidRequestException(
                    "Page size must be between 1 and "
                    + MAXIMUM_PAGE_SIZE
            );
        }

        return PageRequest.of(
                page,
                size,
                sort
        );
    }

    private EvidenceIntelligenceRunResponse
            mapToResponse(

                    EvidenceIntelligenceRun run,
                    List<ExtractedEntity> entities,
                    List<TimelineEvent> events
            ) {

        Evidence evidence = run.getEvidence();

        InvestigationCase investigationCase =
                evidence.getInvestigationCase();

        List<ExtractedEntityResponse> entityResponses =
                entities.stream()
                        .map(this::mapEntity)
                        .toList();

        List<TimelineEventResponse> eventResponses =
                events.stream()
                        .map(this::mapEvent)
                        .toList();

        Long sourceAnalysisId =
                getSourceAnalysisId(run);

        return new EvidenceIntelligenceRunResponse(
                run.getId(),
                evidence.getId(),
                investigationCase.getId(),
                investigationCase.getCaseNumber(),
                evidence.getOriginalFileName(),
                evidence.getFileType(),
                sourceAnalysisId,
                run.getStatus(),
                run.getMethod(),
                run.isHumanReviewRequired(),
                run.getProvider(),
                run.getModel(),
                run.getPromptVersion(),
                run.getResponseSchemaVersion(),
                run.getSourceEvidenceSha256(),
                run.getSourceTextSha256(),
                run.getEntityCount(),
                run.getTimelineEventCount(),
                run.getFailureMessage(),
                entityResponses,
                eventResponses,
                run.getRequestedAt(),
                run.getStartedAt(),
                run.getCompletedAt(),
                run.getUpdatedAt()
        );
    }

    private EvidenceIntelligenceRunSummaryResponse
            mapToSummary(

                    EvidenceIntelligenceRun run
            ) {

        return new EvidenceIntelligenceRunSummaryResponse(
                run.getId(),
                run.getEvidence().getId(),
                getSourceAnalysisId(run),
                run.getStatus(),
                run.getMethod(),
                run.isHumanReviewRequired(),
                run.getProvider(),
                run.getModel(),
                run.getPromptVersion(),
                run.getResponseSchemaVersion(),
                run.getEntityCount(),
                run.getTimelineEventCount(),
                run.getFailureMessage(),
                run.getRequestedAt(),
                run.getStartedAt(),
                run.getCompletedAt(),
                run.getUpdatedAt()
        );
    }

    private Long getSourceAnalysisId(
            EvidenceIntelligenceRun run
    ) {

        return run.getSourceAnalysis() == null
                ? null
                : run.getSourceAnalysis().getId();
    }

    private ExtractedEntityResponse mapEntity(
            ExtractedEntity entity
    ) {

        return new ExtractedEntityResponse(
                entity.getId(),
                entity.getEntityType(),
                entity.getDisplayValue(),
                entity.getNormalizedValue(),
                entity.getContextSnippet(),
                entity.getConfidence(),
                entity.getOccurrenceCount(),
                entity.getFirstCharacterOffset(),
                entity.getLastCharacterOffset()
        );
    }

    private TimelineEventResponse mapEvent(
            TimelineEvent event
    ) {

        List<ExtractedEntityResponse>
                involvedEntities =
                        event.getInvolvedEntities()
                                .stream()
                                .map(this::mapEntity)
                                .toList();

        return new TimelineEventResponse(
                event.getId(),
                event.getSequenceNumber(),
                event.getTitle(),
                event.getDescription(),
                event.getTemporalExpression(),
                event.getNormalizedDateTime(),
                event.getTemporalPrecision(),
                event.getCertainty(),
                event.getContextSnippet(),
                involvedEntities
        );
    }
}