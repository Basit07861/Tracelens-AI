package com.tracelens.evidence.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.evidence.dto.EvidenceExtractionResponse;
import com.tracelens.evidence.entity.Evidence;
import com.tracelens.evidence.entity.EvidenceStatus;
import com.tracelens.evidence.repository.EvidenceRepository;
import com.tracelens.exception.EvidenceNotFoundException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.investigation.entity.InvestigationCase;

@Service
public class EvidenceProcessingStateService {

    private final EvidenceRepository evidenceRepository;

    public EvidenceProcessingStateService(
            EvidenceRepository evidenceRepository
    ) {
        this.evidenceRepository = evidenceRepository;
    }

    @Transactional(readOnly = true)
    public EvidenceProcessingTarget getProcessingTarget(
            Long evidenceId,
            String authenticatedEmail
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        return new EvidenceProcessingTarget(
                evidence.getId(),
                evidence.getFileType(),
                evidence.getStorageRelativePath()
        );
    }

    @Transactional
    public void markProcessing(
            Long evidenceId,
            String authenticatedEmail
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        if (evidence.getStatus()
                == EvidenceStatus.PROCESSING) {

            throw new InvalidRequestException(
                    "Evidence text extraction is "
                    + "already in progress"
            );
        }

        evidence.markProcessing();

        evidenceRepository.saveAndFlush(evidence);
    }

    @Transactional
    public EvidenceExtractionResponse markProcessed(
            Long evidenceId,
            String authenticatedEmail,
            String extractedText
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        evidence.markProcessed(extractedText);

        Evidence savedEvidence =
                evidenceRepository.saveAndFlush(
                        evidence
                );

        return mapToExtractionResponse(
                savedEvidence
        );
    }

    @Transactional
    public void markFailed(
            Long evidenceId,
            String authenticatedEmail,
            String safeErrorMessage
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        evidence.markExtractionFailed(
                safeErrorMessage
        );

        evidenceRepository.saveAndFlush(evidence);
    }

    @Transactional(readOnly = true)
    public EvidenceExtractionResponse
            getExtractionResult(

                    Long evidenceId,
                    String authenticatedEmail
            ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        return mapToExtractionResponse(evidence);
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

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidRequestException(
                    "Authenticated user is unavailable"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private EvidenceExtractionResponse
            mapToExtractionResponse(
                    Evidence evidence
            ) {

        InvestigationCase investigationCase =
                evidence.getInvestigationCase();

        return new EvidenceExtractionResponse(
                evidence.getId(),
                investigationCase.getId(),
                investigationCase.getCaseNumber(),
                evidence.getOriginalFileName(),
                evidence.getFileType(),
                evidence.getStatus(),
                evidence.getExtractedCharacterCount(),
                evidence.getExtractedText(),
                evidence.getExtractionError(),
                evidence.getProcessedAt(),
                evidence.getUpdatedAt()
        );
    }
}