package com.tracelens.ai.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.ai.dto.AiEvidenceAnalysisContent;
import com.tracelens.ai.dto.AiEvidenceAnalysisResponse;
import com.tracelens.ai.entity.AiAnalysisStatus;
import com.tracelens.ai.entity.AiEvidenceAnalysis;
import com.tracelens.ai.repository.AiEvidenceAnalysisRepository;
import com.tracelens.evidence.entity.Evidence;
import com.tracelens.evidence.entity.EvidenceStatus;
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

    private final EvidenceRepository evidenceRepository;

    private final AiEvidenceAnalysisRepository
            analysisRepository;

    public AiEvidenceAnalysisStateService(
            EvidenceRepository evidenceRepository,

            AiEvidenceAnalysisRepository
                    analysisRepository
    ) {
        this.evidenceRepository = evidenceRepository;
        this.analysisRepository = analysisRepository;
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
            String provider,
            String model,
            String promptVersion,
            String responseSchemaVersion,
            String sourceEvidenceSha256,
            String sourceTextSha256
    ) {

        Evidence evidence = evidenceRepository
                .findById(evidenceId)
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
                                "AI analysis record was not found"
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