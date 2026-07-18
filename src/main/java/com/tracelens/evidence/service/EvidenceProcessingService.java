package com.tracelens.evidence.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.tracelens.evidence.config.EvidenceExtractionProperties;
import com.tracelens.evidence.dto.EvidenceExtractionResponse;
import com.tracelens.evidence.dto.EvidenceIntegrityResponse;
import com.tracelens.evidence.extraction.EvidenceTextExtractor;
import com.tracelens.evidence.extraction.EvidenceTextExtractorRegistry;
import com.tracelens.evidence.storage.EvidenceStorageService;
import com.tracelens.exception.EvidenceTextExtractionException;
import com.tracelens.exception.InvalidRequestException;

@Service
public class EvidenceProcessingService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EvidenceProcessingService.class
            );

    private static final String GENERIC_FAILURE_MESSAGE =
            "Evidence text extraction failed unexpectedly";

    private final EvidenceProcessingStateService stateService;
    private final EvidenceTextExtractorRegistry extractorRegistry;
    private final EvidenceStorageService storageService;
    private final EvidenceService evidenceService;
    private final EvidenceExtractionProperties properties;

    public EvidenceProcessingService(
            EvidenceProcessingStateService stateService,
            EvidenceTextExtractorRegistry extractorRegistry,
            EvidenceStorageService storageService,
            EvidenceService evidenceService,
            EvidenceExtractionProperties properties
    ) {
        this.stateService = stateService;
        this.extractorRegistry = extractorRegistry;
        this.storageService = storageService;
        this.evidenceService = evidenceService;
        this.properties = properties;
    }

    public EvidenceExtractionResponse extractText(
            Long evidenceId,
            String authenticatedEmail
    ) {

        EvidenceProcessingTarget target =
                stateService.getProcessingTarget(
                        evidenceId,
                        authenticatedEmail
                );

        /*
         * Resolve the extractor before changing the processing
         * status. PDF extraction is added in Checkpoint 3.
         */
        EvidenceTextExtractor extractor =
                extractorRegistry.getExtractor(
                        target.fileType()
                );

        EvidenceIntegrityResponse integrityResponse =
                evidenceService.verifyEvidenceIntegrity(
                        evidenceId,
                        authenticatedEmail
                );

        if (!integrityResponse.matches()) {
            throw new InvalidRequestException(
                    "Evidence integrity mismatch detected. "
                    + "Text extraction cannot continue."
            );
        }

        stateService.markProcessing(
                evidenceId,
                authenticatedEmail
        );

        try {
            Resource resource =
                    storageService.loadAsResource(
                            target.storageRelativePath()
                    );

            String extractedText =
                    extractor.extract(resource);

            return stateService.markProcessed(
                    evidenceId,
                    authenticatedEmail,
                    extractedText
            );
        }
        catch (EvidenceTextExtractionException exception) {

            String safeMessage =
                    sanitizeErrorMessage(
                            exception.getMessage(),
                            "Evidence content could not "
                            + "be extracted"
                    );

            recordFailureQuietly(
                    evidenceId,
                    authenticatedEmail,
                    safeMessage
            );

            throw exception;
        }
        catch (RuntimeException exception) {

            recordFailureQuietly(
                    evidenceId,
                    authenticatedEmail,
                    GENERIC_FAILURE_MESSAGE
            );

            throw exception;
        }
    }

    public EvidenceExtractionResponse getExtractionResult(
            Long evidenceId,
            String authenticatedEmail
    ) {

        return stateService.getExtractionResult(
                evidenceId,
                authenticatedEmail
        );
    }

    private void recordFailureQuietly(
            Long evidenceId,
            String authenticatedEmail,
            String safeErrorMessage
    ) {

        try {
            stateService.markFailed(
                    evidenceId,
                    authenticatedEmail,
                    safeErrorMessage
            );
        }
        catch (RuntimeException persistenceException) {

            LOGGER.error(
                    "Unable to persist extraction failure "
                    + "status for evidence ID {}",
                    evidenceId,
                    persistenceException
            );
        }
    }

    private String sanitizeErrorMessage(
            String originalMessage,
            String fallbackMessage
    ) {

        String message = originalMessage;

        if (message == null || message.isBlank()) {
            message = fallbackMessage;
        }

        message = message
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .replaceAll("\\s{2,}", " ")
                .trim();

        int maximumLength =
                properties.getMaxErrorMessageLength();

        if (message.length() > maximumLength) {
            return message.substring(
                    0,
                    maximumLength
            );
        }

        return message;
    }
}