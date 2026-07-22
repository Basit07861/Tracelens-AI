package com.tracelens.intelligence.service;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.ai.entity.AiAnalysisStatus;
import com.tracelens.ai.entity.AiEvidenceAnalysis;
import com.tracelens.ai.repository.AiEvidenceAnalysisRepository;
import com.tracelens.evidence.entity.Evidence;
import com.tracelens.exception.EvidenceNotFoundException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.intelligence.entity.EvidenceIntelligenceRun;
import com.tracelens.intelligence.entity.IntelligenceMethod;
import com.tracelens.intelligence.entity.IntelligenceRunRequestType;
import com.tracelens.intelligence.entity.IntelligenceRunStatus;
import com.tracelens.intelligence.repository.EvidenceIntelligenceLockRepository;
import com.tracelens.intelligence.repository.EvidenceIntelligenceRunRepository;

@Service
public class EvidenceIntelligenceRunStartService {

    private static final Set<IntelligenceRunStatus>
            ACTIVE_STATUSES = EnumSet.of(
                    IntelligenceRunStatus.PENDING,
                    IntelligenceRunStatus.PROCESSING
            );

    private final EvidenceIntelligenceLockRepository
            lockRepository;

    private final EvidenceIntelligenceRunRepository
            runRepository;

    private final AiEvidenceAnalysisRepository
            analysisRepository;

    public EvidenceIntelligenceRunStartService(
            EvidenceIntelligenceLockRepository
                    lockRepository,

            EvidenceIntelligenceRunRepository
                    runRepository,

            AiEvidenceAnalysisRepository
                    analysisRepository
    ) {
        this.lockRepository = lockRepository;
        this.runRepository = runRepository;
        this.analysisRepository = analysisRepository;
    }

    /*
     * The evidence row is pessimistically locked before
     * checking run history and saving the new PENDING row.
     *
     * A second request for the same evidence waits for this
     * transaction. After the first transaction commits, the
     * second request detects the active PENDING run and fails
     * without starting another AI request.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public Long createPendingRun(
            Long evidenceId,
            String authenticatedEmail,
            IntelligenceRunRequestType requestType,
            String provider,
            String model,
            String promptVersion,
            String responseSchemaVersion,
            String sourceEvidenceSha256,
            String sourceTextSha256
    ) {

        validateRequestType(requestType);

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        Evidence evidence =
                findAndLockOwnedEvidence(
                        evidenceId,
                        normalizedEmail
                );

        boolean activeRunExists =
                runRepository
                        .existsByEvidenceIdAndStatusIn(
                                evidence.getId(),
                                ACTIVE_STATUSES
                        );

        if (activeRunExists) {
            throw new InvalidRequestException(
                    "An intelligence extraction run is "
                    + "already pending or processing "
                    + "for this evidence"
            );
        }

        boolean previousRunExists =
                runRepository.existsByEvidenceId(
                        evidence.getId()
                );

        validateRequestTypeAgainstHistory(
                requestType,
                previousRunExists
        );

        AiEvidenceAnalysis sourceAnalysis =
                analysisRepository
                        .findFirstByEvidenceIdAndStatusOrderByRequestedAtDesc(
                                evidence.getId(),
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

    private Evidence findAndLockOwnedEvidence(
            Long evidenceId,
            String normalizedEmail
    ) {

        if (evidenceId == null || evidenceId <= 0) {
            throw new EvidenceNotFoundException(
                    "Evidence file was not found"
            );
        }

        return lockRepository
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

    private void validateRequestType(
            IntelligenceRunRequestType requestType
    ) {

        if (requestType == null) {
            throw new InvalidRequestException(
                    "Intelligence run request type is required"
            );
        }
    }

    private void validateRequestTypeAgainstHistory(
            IntelligenceRunRequestType requestType,
            boolean previousRunExists
    ) {

        if (
                requestType
                        == IntelligenceRunRequestType.INITIAL
                && previousRunExists
        ) {
            throw new InvalidRequestException(
                    "Intelligence has already been generated "
                    + "for this evidence. Use the regeneration "
                    + "endpoint to create another run."
            );
        }

        if (
                requestType
                        == IntelligenceRunRequestType.REGENERATION
                && !previousRunExists
        ) {
            throw new InvalidRequestException(
                    "No previous intelligence run exists for "
                    + "this evidence. Generate the initial run "
                    + "before requesting regeneration."
            );
        }
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
}