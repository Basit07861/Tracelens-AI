package com.tracelens.intelligence.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.tracelens.evidence.dto.EvidenceIntegrityResponse;
import com.tracelens.evidence.entity.EvidenceStatus;
import com.tracelens.evidence.service.EvidenceService;
import com.tracelens.exception.AiResponseValidationException;
import com.tracelens.exception.AiServiceUnavailableException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.intelligence.config.IntelligenceExtractionProperties;
import com.tracelens.intelligence.dto.EvidenceIntelligenceContent;
import com.tracelens.intelligence.dto.EvidenceIntelligenceRunResponse;
import com.tracelens.intelligence.dto.IntelligenceEntityContent;
import com.tracelens.intelligence.dto.IntelligenceEntityReferenceContent;
import com.tracelens.intelligence.dto.IntelligenceTimelineEventContent;
import com.tracelens.intelligence.entity.IntelligenceRunRequestType;

@Service
public class EvidenceIntelligenceService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EvidenceIntelligenceService.class
            );

    private static final String SHA_256 =
            "SHA-256";

    private static final String
            VALIDATION_FAILURE_MESSAGE =
                    "The AI service could not produce "
                    + "valid intelligence output";

    private static final String
            PROVIDER_FAILURE_MESSAGE =
                    "The intelligence AI provider "
                    + "was unavailable";

    private static final String
            UNEXPECTED_FAILURE_MESSAGE =
                    "The intelligence extraction "
                    + "could not be completed";

    private final ChatClient chatClient;

    private final EvidenceService evidenceService;

    private final EvidenceIntelligenceStateService
            stateService;

    private final EvidenceIntelligenceRunStartService
            runStartService;

    private final DeterministicEntityExtractor
            deterministicExtractor;

    private final EvidenceIntelligenceValidator validator;

    private final IntelligenceEntityNormalizationService
            normalizationService;

    private final IntelligenceExtractionProperties
            properties;

    private final PromptTemplate promptTemplate;

    private final String provider;

    private final String model;

    public EvidenceIntelligenceService(
            ChatClient traceLensChatClient,

            EvidenceService evidenceService,

            EvidenceIntelligenceStateService
                    stateService,

            EvidenceIntelligenceRunStartService
                    runStartService,

            DeterministicEntityExtractor
                    deterministicExtractor,

            EvidenceIntelligenceValidator validator,

            IntelligenceEntityNormalizationService
                    normalizationService,

            IntelligenceExtractionProperties properties,

            @Value("${app.ai.provider}")
            String provider,

            @Value("${spring.ai.openai.chat.model}")
            String model,

            @Value(
                    "classpath:/prompts/"
                    + "evidence-intelligence-user.st"
            )
            Resource promptResource
    ) {
        this.chatClient = traceLensChatClient;
        this.evidenceService = evidenceService;
        this.stateService = stateService;

        this.runStartService = runStartService;

        this.deterministicExtractor =
                deterministicExtractor;

        this.validator = validator;

        this.normalizationService =
                normalizationService;

        this.properties = properties;
        this.provider = provider;
        this.model = model;

        this.promptTemplate =
                new PromptTemplate(promptResource);
    }

    public EvidenceIntelligenceRunResponse
            generateIntelligence(

                    Long evidenceId,
                    String authenticatedEmail
            ) {

        return runIntelligence(
                evidenceId,
                authenticatedEmail,
                IntelligenceRunRequestType.INITIAL
        );
    }

    public EvidenceIntelligenceRunResponse
            regenerateIntelligence(

                    Long evidenceId,
                    String authenticatedEmail
            ) {

        return runIntelligence(
                evidenceId,
                authenticatedEmail,
                IntelligenceRunRequestType.REGENERATION
        );
    }

    private EvidenceIntelligenceRunResponse
            runIntelligence(

                    Long evidenceId,
                    String authenticatedEmail,
                    IntelligenceRunRequestType requestType
            ) {

        EvidenceIntelligenceTarget target =
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
                    + "Intelligence extraction cannot "
                    + "continue."
            );
        }

        String sourceTextSha256 =
                calculateSha256(
                        target.extractedText()
                );

        Long runId =
                runStartService.createPendingRun(
                        evidenceId,
                        authenticatedEmail,
                        requestType,
                        provider,
                        model,
                        properties.getPromptVersion(),
                        properties.getResponseSchemaVersion(),
                        target.sourceEvidenceSha256(),
                        sourceTextSha256
                );

        try {
            stateService.markProcessing(runId);

            List<IntelligenceEntityCandidate>
                    deterministicEntities =
                            deterministicExtractor.extract(
                                    target.extractedText()
                            );

            String prompt = renderPrompt(
                    target,
                    sourceTextSha256
            );

            EvidenceIntelligenceContent aiContent =
                    generateValidatedContent(
                            runId,
                            prompt,
                            target.extractedText()
                    );

            List<IntelligenceEntityCandidate>
                    combinedEntities =
                            combineEntities(
                                    target.extractedText(),
                                    deterministicEntities,
                                    aiContent.entities()
                            );

            List<IntelligenceTimelineCandidate>
                    timelineCandidates =
                            createTimelineCandidates(
                                    aiContent.timelineEvents()
                            );

            return stateService.markCompleted(
                    runId,
                    combinedEntities,
                    timelineCandidates
            );
        }
        catch (
                AiResponseValidationException exception
        ) {

            recordFailureQuietly(
                    runId,
                    VALIDATION_FAILURE_MESSAGE
            );

            throw exception;
        }
        catch (
                AiServiceUnavailableException exception
        ) {

            recordFailureQuietly(
                    runId,
                    PROVIDER_FAILURE_MESSAGE
            );

            throw exception;
        }
        catch (RuntimeException exception) {

            LOGGER.error(
                    "Unexpected intelligence failure "
                    + "for run ID {} with error type {}",
                    runId,
                    exception
                            .getClass()
                            .getSimpleName()
            );

            recordFailureQuietly(
                    runId,
                    UNEXPECTED_FAILURE_MESSAGE
            );

            throw new AiServiceUnavailableException(
                    "Intelligence extraction service "
                    + "is currently unavailable",
                    exception
            );
        }
    }

    private EvidenceIntelligenceContent
            generateValidatedContent(

                    Long runId,
                    String originalPrompt,
                    String evidenceText
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
                            originalPrompt,
                            attempt,
                            lastValidationException
                    );

            EvidenceIntelligenceContent content =
                    requestModel(
                            runId,
                            attemptPrompt
                    );

            try {
                return validator.validate(
                        content,
                        evidenceText
                );
            }
            catch (
                    AiResponseValidationException exception
            ) {

                lastValidationException = exception;

                LOGGER.warn(
                        "Intelligence validation failed "
                        + "for run ID {} on attempt {}",
                        runId,
                        attempt
                );
            }
        }

        throw new AiResponseValidationException(
                VALIDATION_FAILURE_MESSAGE,
                lastValidationException
        );
    }

    private EvidenceIntelligenceContent requestModel(
            Long runId,
            String prompt
    ) {

        try {
            EvidenceIntelligenceContent content =
                    chatClient
                            .prompt()
                            .user(prompt)
                            .call()
                            .entity(
                                    EvidenceIntelligenceContent.class
                            );

            if (content == null) {
                throw new AiResponseValidationException(
                        "The intelligence response was empty"
                );
            }

            return content;
        }
        catch (
                AiResponseValidationException exception
        ) {
            throw exception;
        }
        catch (RuntimeException exception) {

            LOGGER.error(
                    "Intelligence provider request failed "
                    + "for run ID {} with error type {}",
                    runId,
                    exception
                            .getClass()
                            .getSimpleName()
            );

            throw new AiServiceUnavailableException(
                    "Intelligence extraction service "
                    + "is currently unavailable",
                    exception
            );
        }
    }

    private List<IntelligenceEntityCandidate>
            combineEntities(

                    String evidenceText,

                    List<IntelligenceEntityCandidate>
                            deterministicEntities,

                    List<IntelligenceEntityContent>
                            aiEntities
            ) {

        Map<String, IntelligenceEntityCandidate>
                combined = new LinkedHashMap<>();

        for (
                IntelligenceEntityCandidate candidate
                : deterministicEntities
        ) {

            String key =
                    normalizationService.createKey(
                            candidate.entityType(),
                            candidate.normalizedValue()
                    );

            combined.put(key, candidate);
        }

        for (IntelligenceEntityContent entity : aiEntities) {

            String displayValue =
                    normalizationService
                            .normalizeDisplayValue(
                                    entity.value()
                            );

            String normalizedValue =
                    normalizationService.normalizeValue(
                            entity.entityType(),
                            displayValue
                    );

            String key =
                    normalizationService.createKey(
                            entity.entityType(),
                            normalizedValue
                    );

            int firstOffset = indexOfIgnoreCase(
                    evidenceText,
                    displayValue
            );

            Integer firstCharacterOffset =
                    firstOffset < 0
                            ? null
                            : firstOffset;

            Integer lastCharacterOffset =
                    firstOffset < 0
                            ? null
                            : firstOffset
                                    + displayValue.length();

            int occurrenceCount =
                    countOccurrencesIgnoreCase(
                            evidenceText,
                            displayValue
                    );

            IntelligenceEntityCandidate aiCandidate =
                    new IntelligenceEntityCandidate(
                            entity.entityType(),
                            displayValue,
                            normalizedValue,
                            entity.contextSnippet(),
                            entity.confidence(),
                            Math.max(occurrenceCount, 1),
                            firstCharacterOffset,
                            lastCharacterOffset
                    );

            IntelligenceEntityCandidate existing =
                    combined.get(key);

            if (existing == null) {
                combined.put(key, aiCandidate);
            }
            else {
                combined.put(
                        key,
                        mergeCandidates(
                                existing,
                                aiCandidate
                        )
                );
            }
        }

        if (combined.size()
                > properties.getMaxTotalEntities()) {

            throw new AiResponseValidationException(
                    "Combined intelligence output contains "
                    + "too many entities"
            );
        }

        return List.copyOf(combined.values());
    }

    private IntelligenceEntityCandidate mergeCandidates(
            IntelligenceEntityCandidate first,
            IntelligenceEntityCandidate second
    ) {

        BigDecimal confidence =
                first.confidence()
                        .max(second.confidence());

        String context =
                first.contextSnippet() == null
                        || first.contextSnippet().isBlank()
                        ? second.contextSnippet()
                        : first.contextSnippet();

        Integer firstOffset =
                first.firstCharacterOffset() != null
                        ? first.firstCharacterOffset()
                        : second.firstCharacterOffset();

        Integer lastOffset =
                first.lastCharacterOffset() != null
                        ? first.lastCharacterOffset()
                        : second.lastCharacterOffset();

        return new IntelligenceEntityCandidate(
                first.entityType(),
                first.displayValue(),
                first.normalizedValue(),
                context,
                confidence,
                Math.max(
                        first.occurrenceCount(),
                        second.occurrenceCount()
                ),
                firstOffset,
                lastOffset
        );
    }

    private List<IntelligenceTimelineCandidate>
            createTimelineCandidates(

                    List<IntelligenceTimelineEventContent>
                            values
            ) {

        java.util.ArrayList<
                IntelligenceTimelineCandidate
                > candidates =
                        new java.util.ArrayList<>();

        int sequenceNumber = 1;

        for (
                IntelligenceTimelineEventContent value
                : values
        ) {

            LocalDateTime normalizedDateTime =
                    value.normalizedDateTime() == null
                            || value
                                    .normalizedDateTime()
                                    .isBlank()
                            ? null
                            : LocalDateTime.parse(
                                    value.normalizedDateTime()
                            );

            candidates.add(
                    new IntelligenceTimelineCandidate(
                            sequenceNumber,
                            value.title(),
                            value.description(),
                            value.temporalExpression(),
                            normalizedDateTime,
                            value.temporalPrecision(),
                            value.certainty(),
                            value.contextSnippet(),
                            value.involvedEntities()
                    )
            );

            sequenceNumber++;
        }

        return List.copyOf(candidates);
    }

    private String renderPrompt(
            EvidenceIntelligenceTarget target,
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
                "maxEntityCount",
                properties.getMaxAiEntities()
        );

        variables.put(
                "maxTimelineEventCount",
                properties.getMaxTimelineEvents()
        );

        variables.put(
                "maxValueCharacters",
                properties.getMaxValueCharacters()
        );

        variables.put(
                "maxContextCharacters",
                properties.getMaxContextCharacters()
        );

        variables.put(
                "maxTitleCharacters",
                properties.getMaxTitleCharacters()
        );

        variables.put(
                "maxDescriptionCharacters",
                properties
                        .getMaxDescriptionCharacters()
        );

        variables.put(
                "maxEventEntityLinks",
                properties.getMaxEventEntityLinks()
        );

        return promptTemplate.render(variables);
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



                The previous structured intelligence
                response failed TraceLens validation.

                Validation problem:

                """
                + previousValidationException.getMessage()
                + """



                Correct the structured response. Use only
                values and context supported by the supplied
                evidence text.
                """;
    }

    private void validateTarget(
            EvidenceIntelligenceTarget target
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
                    + "extracted before intelligence "
                    + "processing"
            );
        }

        if (target.extractedText() == null
                || target.extractedText().isBlank()) {

            throw new InvalidRequestException(
                    "The evidence does not contain "
                    + "extracted text for intelligence "
                    + "processing"
            );
        }

        if (target.sourceEvidenceSha256() == null
                || target.sourceEvidenceSha256()
                        .isBlank()) {

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
                    + "the intelligence limit of "
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

    private int indexOfIgnoreCase(
            String source,
            String value
    ) {

        return source
                .toLowerCase(Locale.ROOT)
                .indexOf(
                        value.toLowerCase(Locale.ROOT)
                );
    }

    private int countOccurrencesIgnoreCase(
            String source,
            String value
    ) {

        if (value.isBlank()) {
            return 0;
        }

        Pattern pattern = Pattern.compile(
                Pattern.quote(value),
                Pattern.CASE_INSENSITIVE
                        | Pattern.UNICODE_CASE
        );

        Matcher matcher = pattern.matcher(source);

        int count = 0;

        while (matcher.find()) {
            count++;
        }

        return count;
    }

    private void recordFailureQuietly(
            Long runId,
            String safeMessage
    ) {

        try {
            stateService.markFailed(
                    runId,
                    sanitizeFailureMessage(
                            safeMessage
                    )
            );
        }
        catch (RuntimeException persistenceException) {

            LOGGER.error(
                    "Unable to save FAILED status "
                    + "for intelligence run ID {}",
                    runId
            );
        }
    }

    private String sanitizeFailureMessage(
            String message
    ) {

        String safeMessage =
                message == null
                        ? UNEXPECTED_FAILURE_MESSAGE
                        : normalizationService
                                .normalizeDisplayValue(
                                        message
                                );

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
}