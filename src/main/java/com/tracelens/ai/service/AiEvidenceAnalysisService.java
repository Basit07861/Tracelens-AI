package com.tracelens.ai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.tracelens.ai.config.AiAnalysisProperties;
import com.tracelens.ai.dto.AiEvidenceAnalysisContent;
import com.tracelens.ai.dto.AiEvidenceAnalysisResponse;
import com.tracelens.ai.entity.AiAnalysisRequestType;
import com.tracelens.evidence.dto.EvidenceIntegrityResponse;
import com.tracelens.evidence.entity.EvidenceStatus;
import com.tracelens.evidence.service.EvidenceService;
import com.tracelens.exception.AiResponseValidationException;
import com.tracelens.exception.AiServiceUnavailableException;
import com.tracelens.exception.InvalidRequestException;

@Service
public class AiEvidenceAnalysisService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AiEvidenceAnalysisService.class
            );

    private static final String SHA_256 =
            "SHA-256";

    private static final String
            VALIDATION_FAILURE_MESSAGE =
                    "The AI service could not produce "
                    + "a valid structured analysis";

    private static final String
            PROVIDER_FAILURE_MESSAGE =
                    "The AI analysis provider "
                    + "was unavailable";

    private static final String
            UNEXPECTED_FAILURE_MESSAGE =
                    "The AI analysis could not be completed";

    private final ChatClient chatClient;

    private final EvidenceService evidenceService;

    private final AiEvidenceAnalysisStateService
            stateService;

    private final AiEvidenceAnalysisValidator validator;

    private final AiAnalysisProperties properties;

    private final PromptTemplate analysisPromptTemplate;

    private final String provider;

    private final String model;

    public AiEvidenceAnalysisService(
            ChatClient traceLensChatClient,

            EvidenceService evidenceService,

            AiEvidenceAnalysisStateService
                    stateService,

            AiEvidenceAnalysisValidator validator,

            AiAnalysisProperties properties,

            @Value("${app.ai.provider}")
            String provider,

            @Value("${spring.ai.openai.chat.model}")
            String model,

            @Value(
                    "classpath:/prompts/"
                    + "evidence-analysis-user.st"
            )
            Resource analysisPromptResource
    ) {
        this.chatClient = traceLensChatClient;
        this.evidenceService = evidenceService;
        this.stateService = stateService;
        this.validator = validator;
        this.properties = properties;
        this.provider = provider;
        this.model = model;

        this.analysisPromptTemplate =
                new PromptTemplate(
                        analysisPromptResource
                );
    }

    public AiEvidenceAnalysisResponse generateAnalysis(
            Long evidenceId,
            String authenticatedEmail
    ) {

        return generateAnalysis(
                evidenceId,
                authenticatedEmail,
                AiAnalysisRequestType.INITIAL
        );
    }

    public AiEvidenceAnalysisResponse regenerateAnalysis(
            Long evidenceId,
            String authenticatedEmail
    ) {

        return generateAnalysis(
                evidenceId,
                authenticatedEmail,
                AiAnalysisRequestType.REGENERATION
        );
    }

    private AiEvidenceAnalysisResponse generateAnalysis(
            Long evidenceId,
            String authenticatedEmail,
            AiAnalysisRequestType requestType
    ) {

        AiEvidenceAnalysisTarget target =
                stateService.getTarget(
                        evidenceId,
                        authenticatedEmail
                );

        validateTarget(target);

        EvidenceIntegrityResponse integrity =
                evidenceService.verifyEvidenceIntegrity(
                        evidenceId,
                        authenticatedEmail
                );

        if (!integrity.matches()) {
            throw new InvalidRequestException(
                    "Evidence integrity mismatch detected. "
                    + "AI analysis cannot continue."
            );
        }

        String sourceTextSha256 =
                calculateSha256(
                        target.extractedText()
                );

        Long analysisId =
                stateService.createPendingAnalysis(
                        evidenceId,
                        authenticatedEmail,
                        requestType,
                        provider,
                        model,
                        properties.getPromptVersion(),
                        properties
                                .getResponseSchemaVersion(),
                        target.sourceEvidenceSha256(),
                        sourceTextSha256
                );

        try {
            stateService.markProcessing(analysisId);

            String prompt = renderPrompt(
                    target,
                    sourceTextSha256
            );

            ValidatedModelResult modelResult =
                    generateValidatedAnalysis(
                            analysisId,
                            prompt
                    );

            return stateService.markCompleted(
                    analysisId,
                    modelResult.content(),
                    modelResult.promptTokens(),
                    modelResult.completionTokens(),
                    modelResult.totalTokens()
            );
        }
        catch (
                AiResponseValidationException exception
        ) {

            recordFailureQuietly(
                    analysisId,
                    VALIDATION_FAILURE_MESSAGE
            );

            throw exception;
        }
        catch (
                AiServiceUnavailableException exception
        ) {

            recordFailureQuietly(
                    analysisId,
                    PROVIDER_FAILURE_MESSAGE
            );

            throw exception;
        }
        catch (RuntimeException exception) {

            LOGGER.error(
                    "Unexpected AI analysis failure for "
                    + "analysis ID {} with error type {}",
                    analysisId,
                    exception
                            .getClass()
                            .getSimpleName()
            );

            recordFailureQuietly(
                    analysisId,
                    UNEXPECTED_FAILURE_MESSAGE
            );

            throw new AiServiceUnavailableException(
                    "AI analysis service is currently "
                    + "unavailable",
                    exception
            );
        }
    }

    private ValidatedModelResult
            generateValidatedAnalysis(

                    Long analysisId,
                    String originalPrompt
            ) {

        AiResponseValidationException
                lastValidationException = null;

        int totalPromptTokens = 0;
        int totalCompletionTokens = 0;
        int totalTokens = 0;

        boolean tokenUsageAvailable = false;

        int maximumAttempts =
                properties.getValidationAttempts();

        for (
                int attempt = 1;
                attempt <= maximumAttempts;
                attempt++
        ) {

            String attemptPrompt = buildAttemptPrompt(
                    originalPrompt,
                    attempt,
                    lastValidationException
            );

            ModelResult modelResult = requestModel(
                    analysisId,
                    attemptPrompt
            );

            if (modelResult.promptTokens() != null) {
                totalPromptTokens +=
                        modelResult.promptTokens();

                tokenUsageAvailable = true;
            }

            if (modelResult.completionTokens() != null) {
                totalCompletionTokens +=
                        modelResult.completionTokens();

                tokenUsageAvailable = true;
            }

            if (modelResult.totalTokens() != null) {
                totalTokens += modelResult.totalTokens();
                tokenUsageAvailable = true;
            }

            try {
                AiEvidenceAnalysisContent
                        validatedContent =
                                validator.validate(
                                        modelResult.content()
                                );

                return new ValidatedModelResult(
                        validatedContent,
                        tokenUsageAvailable
                                ? totalPromptTokens
                                : null,
                        tokenUsageAvailable
                                ? totalCompletionTokens
                                : null,
                        tokenUsageAvailable
                                ? totalTokens
                                : null
                );
            }
            catch (
                    AiResponseValidationException exception
            ) {

                lastValidationException = exception;

                LOGGER.warn(
                        "AI analysis validation failed "
                        + "for analysis ID {} "
                        + "on attempt {}",
                        analysisId,
                        attempt
                );
            }
        }

        throw new AiResponseValidationException(
                VALIDATION_FAILURE_MESSAGE,
                lastValidationException
        );
    }

    private ModelResult requestModel(
            Long analysisId,
            String prompt
    ) {

        try {
            ResponseEntity<
                    ChatResponse,
                    AiEvidenceAnalysisContent
                    > responseEntity =
                            chatClient
                                    .prompt()
                                    .user(prompt)
                                    .call()
                                    .responseEntity(
                                            AiEvidenceAnalysisContent.class
                                    );

            AiEvidenceAnalysisContent content =
                    responseEntity.entity();

            if (content == null) {
                throw new AiResponseValidationException(
                        "The AI analysis response was empty"
                );
            }

            Usage usage = extractUsage(
                    responseEntity.response()
            );

            return new ModelResult(
                    content,
                    usage == null
                            ? null
                            : usage.getPromptTokens(),
                    usage == null
                            ? null
                            : usage.getCompletionTokens(),
                    usage == null
                            ? null
                            : usage.getTotalTokens()
            );
        }
        catch (
                AiResponseValidationException exception
        ) {
            throw exception;
        }
        catch (RuntimeException exception) {

            LOGGER.error(
                    "AI provider request failed for "
                    + "analysis ID {} with error type {}",
                    analysisId,
                    exception
                            .getClass()
                            .getSimpleName()
            );

            throw new AiServiceUnavailableException(
                    "AI analysis service is currently "
                    + "unavailable",
                    exception
            );
        }
    }

    private Usage extractUsage(
            ChatResponse chatResponse
    ) {

        if (chatResponse == null
                || chatResponse.getMetadata() == null) {

            return null;
        }

        return chatResponse
                .getMetadata()
                .getUsage();
    }

    private String renderPrompt(
            AiEvidenceAnalysisTarget target,
            String sourceTextSha256
    ) {

        Map<String, Object> variables =
                new LinkedHashMap<>();

        variables.put(
                "fileName",
                target.originalFileName()
        );

        variables.put(
                "fileType",
                target.fileType().name()
        );

        variables.put(
                "caseNumber",
                target.caseNumber()
        );

        variables.put(
                "evidenceSha256",
                target.sourceEvidenceSha256()
        );

        variables.put(
                "textSha256",
                sourceTextSha256
        );

        variables.put(
                "evidenceText",
                target.extractedText()
        );

        variables.put(
                "maxSummaryCharacters",
                properties.getMaxSummaryCharacters()
        );

        variables.put(
                "maxFindingCount",
                properties.getMaxFindings()
        );

        variables.put(
                "maxActionCount",
                properties.getMaxActions()
        );

        variables.put(
                "maxLimitationCount",
                properties.getMaxLimitations()
        );

        variables.put(
                "maxItemCharacters",
                properties.getMaxItemCharacters()
        );

        return analysisPromptTemplate.render(
                variables
        );
    }

    private String buildAttemptPrompt(
            String originalPrompt,
            int attempt,
            AiResponseValidationException
                    previousValidationException
    ) {

        if (attempt == 1
                || previousValidationException == null) {

            return originalPrompt;
        }

        return originalPrompt
                + """



                The previous structured response failed
                TraceLens application validation.

                Validation problem:

                """
                + previousValidationException.getMessage()
                + """



                Correct the structured response and follow
                every requested limit and rule.
                """;
    }

    private void validateTarget(
            AiEvidenceAnalysisTarget target
    ) {

        if (target == null) {
            throw new InvalidRequestException(
                    "Evidence information is unavailable"
            );
        }

        if (target.evidenceStatus()
                != EvidenceStatus.PROCESSED) {

            throw new InvalidRequestException(
                    "Evidence text must be successfully "
                    + "extracted before AI analysis"
            );
        }

        if (target.extractedText() == null
                || target.extractedText().isBlank()) {

            throw new InvalidRequestException(
                    "The evidence does not contain "
                    + "extracted text for AI analysis"
            );
        }

        if (target.sourceEvidenceSha256() == null
                || target.sourceEvidenceSha256().isBlank()) {

            throw new InvalidRequestException(
                    "The evidence does not contain "
                    + "a SHA-256 integrity baseline"
            );
        }

        int maximumCharacters =
                properties.getMaxInputCharacters();

        if (target.extractedText().length()
                > maximumCharacters) {

            throw new InvalidRequestException(
                    "Extracted evidence text exceeds "
                    + "the AI analysis limit of "
                    + maximumCharacters
                    + " characters"
            );
        }
    }

    private String calculateSha256(
            String value
    ) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            SHA_256
                    );

            byte[] hash = digest.digest(
                    value.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            return HexFormat
                    .of()
                    .formatHex(hash);
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 hashing is unavailable",
                    exception
            );
        }
    }

    private void recordFailureQuietly(
            Long analysisId,
            String safeMessage
    ) {

        try {
            stateService.markFailed(
                    analysisId,
                    sanitizeFailureMessage(
                            safeMessage
                    )
            );
        }
        catch (RuntimeException persistenceException) {

            LOGGER.error(
                    "Unable to save FAILED status for "
                    + "analysis ID {}",
                    analysisId
            );
        }
    }

    private String sanitizeFailureMessage(
            String message
    ) {

        String safeMessage =
                message == null
                        ? UNEXPECTED_FAILURE_MESSAGE
                        : message
                                .replace('\r', ' ')
                                .replace('\n', ' ')
                                .replace('\t', ' ')
                                .replaceAll(
                                        "\\s{2,}",
                                        " "
                                )
                                .strip();

        int maximumLength =
                properties
                        .getMaxFailureMessageCharacters();

        if (safeMessage.length() > maximumLength) {
            return safeMessage.substring(
                    0,
                    maximumLength
            );
        }

        return safeMessage;
    }

    private record ModelResult(
            AiEvidenceAnalysisContent content,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens
    ) {
    }

    private record ValidatedModelResult(
            AiEvidenceAnalysisContent content,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens
    ) {
    }
}