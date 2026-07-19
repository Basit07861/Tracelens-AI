package com.tracelens.ai.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.tracelens.ai.config.AiPreviewProperties;
import com.tracelens.ai.dto.AiEvidencePreviewContent;
import com.tracelens.ai.dto.AiEvidencePreviewResponse;
import com.tracelens.evidence.dto.EvidenceExtractionResponse;
import com.tracelens.evidence.entity.EvidenceStatus;
import com.tracelens.evidence.service.EvidenceProcessingService;
import com.tracelens.exception.AiResponseValidationException;
import com.tracelens.exception.AiServiceUnavailableException;
import com.tracelens.exception.InvalidRequestException;

@Service
public class AiEvidencePreviewService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AiEvidencePreviewService.class
            );

    private final ChatClient chatClient;

    private final EvidenceProcessingService
            evidenceProcessingService;

    private final PromptTemplate previewPromptTemplate;

    private final AiPreviewProperties properties;

    private final AiEvidencePreviewValidator validator;

    public AiEvidencePreviewService(
            ChatClient traceLensChatClient,

            EvidenceProcessingService
                    evidenceProcessingService,

            AiPreviewProperties properties,

            AiEvidencePreviewValidator validator,

            @Value(
                    "classpath:/prompts/"
                    + "evidence-preview-user.st"
            )
            Resource previewPromptResource
    ) {
        this.chatClient = traceLensChatClient;

        this.evidenceProcessingService =
                evidenceProcessingService;

        this.properties = properties;
        this.validator = validator;

        this.previewPromptTemplate =
                new PromptTemplate(
                        previewPromptResource
                );
    }

    public AiEvidencePreviewResponse generatePreview(
            Long evidenceId,
            String authenticatedEmail
    ) {

        EvidenceExtractionResponse extraction =
                evidenceProcessingService
                        .getExtractionResult(
                                evidenceId,
                                authenticatedEmail
                        );

        validateExtraction(extraction);

        String renderedPrompt =
                renderPrompt(extraction);

        AiEvidencePreviewContent validatedPreview =
                generateValidatedPreview(
                        evidenceId,
                        renderedPrompt
                );

        return createResponse(
                extraction,
                validatedPreview
        );
    }

    private String renderPrompt(
            EvidenceExtractionResponse extraction
    ) {

        return previewPromptTemplate.render(
                Map.of(
                        "fileName",
                        extraction.originalFileName(),

                        "fileType",
                        extraction.fileType().name(),

                        "caseNumber",
                        extraction.caseNumber(),

                        "evidenceText",
                        extraction.extractedText(),

                        "maxSummaryCharacters",
                        properties
                                .getMaxSummaryCharacters(),

                        "maxIndicatorCount",
                        properties.getMaxIndicators(),

                        "maxLimitationCount",
                        properties.getMaxLimitations(),

                        "maxListItemCharacters",
                        properties
                                .getMaxListItemCharacters()
                )
        );
    }

    private AiEvidencePreviewContent
            generateValidatedPreview(

                    Long evidenceId,
                    String renderedPrompt
            ) {

        AiResponseValidationException
                lastValidationException = null;

        int maximumAttempts =
                properties.getValidationAttempts();

        for (
                int attempt = 1;
                attempt <= maximumAttempts;
                attempt++
        ) {

            String attemptPrompt =
                    buildAttemptPrompt(
                            renderedPrompt,
                            attempt,
                            lastValidationException
                    );

            AiEvidencePreviewContent preview =
                    requestPreview(
                            evidenceId,
                            attemptPrompt
                    );

            try {
                return validator.validate(preview);
            }
            catch (
                    AiResponseValidationException exception
            ) {

                lastValidationException = exception;

                LOGGER.warn(
                        "AI preview validation failed "
                        + "for evidence ID {} on attempt {}",
                        evidenceId,
                        attempt
                );
            }
        }

        throw new AiResponseValidationException(
                "The AI service could not produce "
                + "a valid structured preview",
                lastValidationException
        );
    }

    private AiEvidencePreviewContent requestPreview(
            Long evidenceId,
            String prompt
    ) {

        try {
            return chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .entity(
                            AiEvidencePreviewContent.class
                    );
        }
        catch (RuntimeException exception) {

            LOGGER.error(
                    "AI preview request failed for "
                    + "evidence ID {} with error type {}",
                    evidenceId,
                    exception
                            .getClass()
                            .getSimpleName()
            );

            throw new AiServiceUnavailableException(
                    "AI preview service is currently "
                    + "unavailable",
                    exception
            );
        }
    }

    private String buildAttemptPrompt(
            String originalPrompt,
            int attempt,
            AiResponseValidationException
                    previousValidationException
    ) {

        if (
                attempt == 1
                || previousValidationException == null
        ) {
            return originalPrompt;
        }

        return originalPrompt
                + """



                The previous structured response failed
                application validation.

                Validation problem:

                """
                + previousValidationException.getMessage()
                + """



                Correct the response and return a complete
                structured result that follows every rule.
                """;
    }

    private void validateExtraction(
            EvidenceExtractionResponse extraction
    ) {

        if (extraction == null) {
            throw new InvalidRequestException(
                    "Evidence extraction information "
                    + "is unavailable"
            );
        }

        if (
                extraction.status()
                != EvidenceStatus.PROCESSED
        ) {
            throw new InvalidRequestException(
                    "Evidence text must be successfully "
                    + "extracted before generating "
                    + "an AI preview"
            );
        }

        String extractedText =
                extraction.extractedText();

        if (
                extractedText == null
                || extractedText.isBlank()
        ) {
            throw new InvalidRequestException(
                    "The evidence does not contain "
                    + "extracted text for AI analysis"
            );
        }

        int maximumInputCharacters =
                properties.getMaxInputCharacters();

        if (
                extractedText.length()
                > maximumInputCharacters
        ) {
            throw new InvalidRequestException(
                    "Extracted evidence text exceeds "
                    + "the AI preview limit of "
                    + maximumInputCharacters
                    + " characters"
            );
        }
    }

    private AiEvidencePreviewResponse createResponse(
            EvidenceExtractionResponse extraction,
            AiEvidencePreviewContent preview
    ) {

        List<String> keyIndicators =
                preview.keyIndicators() == null
                        ? List.of()
                        : preview.keyIndicators();

        List<String> limitations =
                preview.limitations() == null
                        ? List.of()
                        : preview.limitations();

        return new AiEvidencePreviewResponse(
                extraction.evidenceId(),
                extraction.caseId(),
                extraction.caseNumber(),
                extraction.originalFileName(),
                extraction.fileType(),
                preview.summary(),
                preview.riskLevel(),
                keyIndicators,
                Boolean.TRUE.equals(
                        preview.sufficientInformation()
                ),
                limitations,
                true,
                Instant.now()
        );
    }
}