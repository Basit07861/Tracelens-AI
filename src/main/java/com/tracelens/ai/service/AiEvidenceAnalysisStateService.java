package com.tracelens.ai.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.ai.dto.AiEvidenceAnalysisContent;
import com.tracelens.ai.dto.AiEvidenceAnalysisHistoryResponse;
import com.tracelens.ai.dto.AiEvidenceAnalysisResponse;
import com.tracelens.ai.entity.AiAnalysisRequestType;
import com.tracelens.ai.entity.AiAnalysisStatus;
import com.tracelens.ai.entity.AiEvidenceAnalysis;
import com.tracelens.ai.repository.AiEvidenceAnalysisLockRepository;
import com.tracelens.ai.repository.AiEvidenceAnalysisRepository;
import com.tracelens.evidence.entity.Evidence;
import com.tracelens.evidence.repository.EvidenceRepository;
import com.tracelens.exception.EvidenceNotFoundException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.investigation.entity.InvestigationCase;

@Service
public class AiEvidenceAnalysisStateService {

    private static final Set<AiAnalysisStatus>
            ACTIVE_STATUSES = EnumSet.of(
                    AiAnalysisStatus.PENDING,
                    AiAnalysisStatus.PROCESSING
            );

    private static final int MAXIMUM_HISTORY_PAGE_SIZE =
            50;

    private final EvidenceRepository evidenceRepository;

    private final AiEvidenceAnalysisRepository
            analysisRepository;

    private final AiEvidenceAnalysisLockRepository
            analysisLockRepository;

    public AiEvidenceAnalysisStateService(
            EvidenceRepository evidenceRepository,

            AiEvidenceAnalysisRepository
                    analysisRepository,

            AiEvidenceAnalysisLockRepository
                    analysisLockRepository
    ) {
        this.evidenceRepository = evidenceRepository;
        this.analysisRepository = analysisRepository;
        this.analysisLockRepository =
                analysisLockRepository;
    }

    @Transactional(readOnly = true)
    public AiEvidenceAnalysisTarget getTarget(
            Long evidenceId,
            String authenticatedEmail
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        InvestigationCase investigationCase =
                evidence.getInvestigationCase();

        return new AiEvidenceAnalysisTarget(
                evidence.getId(),
                investigationCase.getId(),
                investigationCase.getCaseNumber(),
                evidence.getOriginalFileName(),
                evidence.getFileType(),
                evidence.getStatus(),
                evidence.getIntegrityStatus(),
                evidence.getSha256Hash(),
                evidence.getExtractedText()
        );
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public Long createPendingAnalysis(
            Long evidenceId,
            String authenticatedEmail,
            AiAnalysisRequestType requestType,
            String provider,
            String model,
            String promptVersion,
            String responseSchemaVersion,
            String sourceEvidenceSha256,
            String sourceTextSha256
    ) {

        if (requestType == null) {
            throw new InvalidRequestException(
                    "AI analysis request type is unavailable"
            );
        }

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        Evidence evidence = analysisLockRepository
                .findOwnedEvidenceForUpdate(
                        evidenceId,
                        normalizedEmail
                )
                .orElseThrow(
                        () -> new EvidenceNotFoundException(
                                "Evidence file was not found"
                        )
                );

        boolean activeAnalysisExists =
                analysisRepository
                        .existsByEvidenceIdAndStatusIn(
                                evidenceId,
                                ACTIVE_STATUSES
                        );

        if (activeAnalysisExists) {
            throw new InvalidRequestException(
                    "An AI analysis is already pending "
                    + "or processing for this evidence"
            );
        }

        long previousAnalysisCount =
                analysisRepository.countByEvidenceId(
                        evidenceId
                );

        validateRequestType(
                requestType,
                previousAnalysisCount
        );

        AiEvidenceAnalysis analysis =
                new AiEvidenceAnalysis();

        analysis.setEvidence(evidence);
        analysis.setStatus(AiAnalysisStatus.PENDING);
        analysis.setProvider(provider);
        analysis.setModel(model);
        analysis.setPromptVersion(promptVersion);

        analysis.setResponseSchemaVersion(
                responseSchemaVersion
        );

        analysis.setSourceEvidenceSha256(
                sourceEvidenceSha256
        );

        analysis.setSourceTextSha256(
                sourceTextSha256
        );

        AiEvidenceAnalysis savedAnalysis =
                analysisRepository.saveAndFlush(
                        analysis
                );

        return savedAnalysis.getId();
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markProcessing(
            Long analysisId
    ) {

        AiEvidenceAnalysis analysis =
                findAnalysis(analysisId);

        analysis.markProcessing();

        analysisRepository.saveAndFlush(analysis);
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiEvidenceAnalysisResponse markCompleted(
            Long analysisId,
            AiEvidenceAnalysisContent content,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens
    ) {

        AiEvidenceAnalysis analysis =
                findAnalysis(analysisId);

        analysis.markCompleted(
                content.summary(),
                content.riskLevel(),
                content.suspiciousFindings(),
                content.recommendedActions(),
                content.sufficientInformation(),
                content.limitations(),
                promptTokens,
                completionTokens,
                totalTokens
        );

        AiEvidenceAnalysis savedAnalysis =
                analysisRepository.saveAndFlush(
                        analysis
                );

        return mapToResponse(savedAnalysis);
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markFailed(
            Long analysisId,
            String safeFailureMessage
    ) {

        AiEvidenceAnalysis analysis =
                findAnalysis(analysisId);

        analysis.markFailed(safeFailureMessage);

        analysisRepository.saveAndFlush(analysis);
    }

    @Transactional(readOnly = true)
    public AiEvidenceAnalysisResponse getAnalysis(
            Long analysisId,
            String authenticatedEmail
    ) {

        if (analysisId == null || analysisId <= 0) {
            throw new EvidenceNotFoundException(
                    "AI evidence analysis was not found"
            );
        }

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        AiEvidenceAnalysis analysis =
                analysisRepository
                        .findByIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                                analysisId,
                                normalizedEmail
                        )
                        .orElseThrow(
                                () -> new EvidenceNotFoundException(
                                        "AI evidence analysis "
                                        + "was not found"
                                )
                        );

        return mapToResponse(analysis);
    }

    @Transactional(readOnly = true)
    public AiEvidenceAnalysisResponse getLatestAnalysis(
            Long evidenceId,
            String authenticatedEmail
    ) {

        findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        AiEvidenceAnalysis analysis =
                analysisRepository
                        .findFirstByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCaseOrderByRequestedAtDesc(
                                evidenceId,
                                normalizedEmail
                        )
                        .orElseThrow(
                                () -> new EvidenceNotFoundException(
                                        "No AI analysis was found "
                                        + "for this evidence"
                                )
                        );

        return mapToResponse(analysis);
    }

    @Transactional(readOnly = true)
    public AiEvidenceAnalysisHistoryResponse
            getAnalysisHistory(

                    Long evidenceId,
                    String authenticatedEmail,
                    int page,
                    int size
            ) {

        validatePagination(page, size);

        findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "requestedAt"
                )
        );

        Page<AiEvidenceAnalysis> analysisPage =
                analysisRepository
                        .findAllByEvidenceIdAndEvidenceInvestigationCaseOwnerEmailIgnoreCase(
                                evidenceId,
                                normalizedEmail,
                                pageRequest
                        );

        List<AiEvidenceAnalysisResponse> content =
                analysisPage
                        .getContent()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return new AiEvidenceAnalysisHistoryResponse(
                evidenceId,
                content,
                analysisPage.getNumber(),
                analysisPage.getSize(),
                analysisPage.getTotalElements(),
                analysisPage.getTotalPages(),
                analysisPage.getNumberOfElements(),
                analysisPage.isFirst(),
                analysisPage.isLast()
        );
    }

    private void validateRequestType(
            AiAnalysisRequestType requestType,
            long previousAnalysisCount
    ) {

        if (requestType
                == AiAnalysisRequestType.INITIAL
                && previousAnalysisCount > 0) {

            throw new InvalidRequestException(
                    "An AI analysis already exists for "
                    + "this evidence. Use the regeneration "
                    + "endpoint to create another analysis."
            );
        }

        if (requestType
                == AiAnalysisRequestType.REGENERATION
                && previousAnalysisCount == 0) {

            throw new InvalidRequestException(
                    "No previous AI analysis exists for "
                    + "this evidence. Use the initial "
                    + "analysis endpoint first."
            );
        }
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new InvalidRequestException(
                    "Analysis-history page cannot "
                    + "be negative"
            );
        }

        if (size <= 0
                || size > MAXIMUM_HISTORY_PAGE_SIZE) {

            throw new InvalidRequestException(
                    "Analysis-history size must be "
                    + "between 1 and "
                    + MAXIMUM_HISTORY_PAGE_SIZE
            );
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

    private AiEvidenceAnalysis findAnalysis(
            Long analysisId
    ) {

        return analysisRepository
                .findById(analysisId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "AI analysis record "
                                + "was not found"
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

    private AiEvidenceAnalysisResponse mapToResponse(
            AiEvidenceAnalysis analysis
    ) {

        Evidence evidence = analysis.getEvidence();

        InvestigationCase investigationCase =
                evidence.getInvestigationCase();

        return new AiEvidenceAnalysisResponse(
                analysis.getId(),
                evidence.getId(),
                investigationCase.getId(),
                investigationCase.getCaseNumber(),
                evidence.getOriginalFileName(),
                evidence.getFileType(),
                analysis.getStatus(),
                analysis.getSummary(),
                analysis.getRiskLevel(),
                List.copyOf(
                        analysis.getSuspiciousFindings()
                ),
                List.copyOf(
                        analysis.getRecommendedActions()
                ),
                analysis.getSufficientInformation(),
                List.copyOf(
                        analysis.getLimitations()
                ),
                analysis.isHumanReviewRequired(),
                analysis.getProvider(),
                analysis.getModel(),
                analysis.getPromptVersion(),
                analysis.getResponseSchemaVersion(),
                analysis.getSourceEvidenceSha256(),
                analysis.getSourceTextSha256(),
                analysis.getPromptTokens(),
                analysis.getCompletionTokens(),
                analysis.getTotalTokens(),
                analysis.getFailureMessage(),
                analysis.getRequestedAt(),
                analysis.getStartedAt(),
                analysis.getCompletedAt(),
                analysis.getUpdatedAt()
        );
    }
}