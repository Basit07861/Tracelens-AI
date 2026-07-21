package com.tracelens.intelligence.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.tracelens.exception.AiResponseValidationException;
import com.tracelens.intelligence.config.IntelligenceExtractionProperties;
import com.tracelens.intelligence.dto.EvidenceIntelligenceContent;
import com.tracelens.intelligence.dto.IntelligenceEntityContent;
import com.tracelens.intelligence.dto.IntelligenceEntityReferenceContent;
import com.tracelens.intelligence.dto.IntelligenceTimelineEventContent;
import com.tracelens.intelligence.entity.ExtractedEntityType;
import com.tracelens.intelligence.entity.TimelineEventCertainty;
import com.tracelens.intelligence.entity.TimelineTemporalPrecision;

@Component
public class EvidenceIntelligenceValidator {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO;

    private static final BigDecimal ONE =
            BigDecimal.ONE;

    private static final BigDecimal DEFAULT_AI_CONFIDENCE =
            new BigDecimal("0.7500");

    private static final Set<String> CURRENCY_CODES =
            Set.of(
                    "inr",
                    "usd",
                    "eur",
                    "gbp",
                    "rs",
                    "rs."
            );

    private static final Pattern MASKED_ACCOUNT_PATTERN =
            Pattern.compile(
                    "(?i)^(?:x{3,}|\\*{3,})\\d{2,}$"
            );

    private static final Pattern COMPACT_TIMESTAMP_PATTERN =
            Pattern.compile(
                    "^\\d{8}[-_]?\\d{6}$"
            );

    private static final Pattern ALL_DIGITS_PATTERN =
            Pattern.compile("^\\d+$");

    /*
     * Valid:
     * 80000
     * 80,000
     * 80000.50
     * INR 80000
     * 80000 INR
     * Rs. 80,000
     * ₹80,000
     * $500
     *
     * Invalid:
     * XXXX1234
     * account1234
     * TX-80000
     */
    private static final Pattern MONEY_VALUE_PATTERN =
            Pattern.compile(
                    "(?i)^"
                    + "(?:(?:₹|Rs\\.?|INR|USD|EUR|GBP|"
                    + "\\$|€|£)\\s*)?"
                    + "\\d[\\d,]*"
                    + "(?:\\.\\d{1,2})?"
                    + "(?:\\s*(?:INR|USD|EUR|GBP))?"
                    + "$"
            );

    /*
     * Words that usually identify a business,
     * institution or organisation.
     */
    private static final Pattern ORGANIZATION_INDICATOR_PATTERN =
            Pattern.compile(
                    "(?i)\\b(?:"
                    + "vendor"
                    + "|company"
                    + "|technologies"
                    + "|technology"
                    + "|solutions"
                    + "|services"
                    + "|enterprise"
                    + "|enterprises"
                    + "|corporation"
                    + "|corp"
                    + "|inc"
                    + "|limited"
                    + "|ltd"
                    + "|llp"
                    + "|private"
                    + "|pvt"
                    + "|bank"
                    + "|merchant"
                    + "|agency"
                    + "|department"
                    + "|ministry"
                    + "|university"
                    + "|college"
                    + "|hospital"
                    + "|foundation"
                    + "|association"
                    + "|trust"
                    + "|group"
                    + "|store"
                    + "|shop"
                    + ")\\b"
            );

    /*
     * Review flags, status descriptions and action phrases
     * are not people or organisations.
     */
    private static final Pattern NON_ENTITY_STATUS_PHRASE_PATTERN =
            Pattern.compile(
                    "(?i)^(?:"
                    + "account\\s+(?:change|changed)"
                    + "|urgent\\s+payment\\s+request"
                    + "|payment\\s+request"
                    + "|review\\s+flag"
                    + "|status\\s+flag"
                    + "|account\\s+change\\s+review\\s+flag"
                    + "|urgent\\s+payment\\s+request\\s+flag"
                    + ")$"
            );

    private final IntelligenceExtractionProperties properties;

    private final IntelligenceEntityNormalizationService
            normalizationService;

    public EvidenceIntelligenceValidator(
            IntelligenceExtractionProperties properties,
            IntelligenceEntityNormalizationService
                    normalizationService
    ) {
        this.properties = properties;
        this.normalizationService = normalizationService;
    }

    public EvidenceIntelligenceContent validate(
            EvidenceIntelligenceContent content,
            String evidenceText
    ) {

        if (content == null) {
            throw new AiResponseValidationException(
                    "The intelligence response was empty"
            );
        }

        List<IntelligenceEntityContent> entities =
                normalizeEntities(
                        content.entities(),
                        evidenceText
                );

        List<IntelligenceTimelineEventContent> timelineEvents =
                normalizeTimelineEvents(
                        content.timelineEvents(),
                        evidenceText
                );

        return new EvidenceIntelligenceContent(
                entities,
                timelineEvents
        );
    }

    private List<IntelligenceEntityContent> normalizeEntities(
            List<IntelligenceEntityContent> values,
            String evidenceText
    ) {

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        if (values.size() > properties.getMaxAiEntities()) {
            throw new AiResponseValidationException(
                    "The intelligence response contains "
                    + "too many entity candidates"
            );
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(value -> normalizeEntity(
                        value,
                        evidenceText
                ))
                .filter(Objects::nonNull)
                .toList();
    }

    private IntelligenceEntityContent normalizeEntity(
            IntelligenceEntityContent value,
            String evidenceText
    ) {

        if (value.entityType() == null) {
            throw new AiResponseValidationException(
                    "An intelligence entity is missing "
                    + "its type"
            );
        }

        String displayValue =
                requireText(
                        value.value(),
                        "An intelligence entity is missing "
                        + "its value",
                        properties.getMaxValueCharacters()
                );

        String contextSnippet =
                requireText(
                        value.contextSnippet(),
                        "An intelligence entity is missing "
                        + "its evidence context",
                        properties.getMaxContextCharacters()
                );

        requireEvidenceSupport(
                evidenceText,
                displayValue,
                "An intelligence entity value is not "
                + "supported by the evidence"
        );

        requireEvidenceSupport(
                evidenceText,
                contextSnippet,
                "An intelligence entity context is not "
                + "supported by the evidence"
        );

        BigDecimal confidence =
                value.confidence() == null
                        ? DEFAULT_AI_CONFIDENCE
                        : value.confidence();

        if (
                confidence.compareTo(ZERO) < 0
                || confidence.compareTo(ONE) > 0
        ) {
            throw new AiResponseValidationException(
                    "Entity confidence must be between "
                    + "0.0 and 1.0"
            );
        }

        ExtractedEntityType correctedType =
                correctEntityType(
                        value.entityType(),
                        displayValue
                );

        if (
                !isSemanticallyPlausible(
                        correctedType,
                        displayValue
                )
        ) {
            return null;
        }

        return new IntelligenceEntityContent(
                correctedType,
                displayValue,
                contextSnippet,
                confidence
        );
    }

    private ExtractedEntityType correctEntityType(
            ExtractedEntityType originalType,
            String displayValue
    ) {

        if (
                originalType == ExtractedEntityType.PERSON
                && looksLikeOrganization(displayValue)
        ) {
            return ExtractedEntityType.ORGANIZATION;
        }

        return originalType;
    }

    private boolean looksLikeOrganization(
            String value
    ) {

        return ORGANIZATION_INDICATOR_PATTERN
                .matcher(value)
                .find();
    }

    private boolean isNonEntityStatusPhrase(
            String value
    ) {

        return NON_ENTITY_STATUS_PHRASE_PATTERN
                .matcher(value.strip())
                .matches();
    }

    private boolean isSemanticallyPlausible(
            ExtractedEntityType type,
            String displayValue
    ) {

        String normalized =
                displayValue
                        .strip()
                        .toLowerCase(Locale.ROOT);

        return switch (type) {

            case ORGANIZATION ->
                    !CURRENCY_CODES.contains(normalized)
                    && !MASKED_ACCOUNT_PATTERN
                            .matcher(displayValue)
                            .matches()
                    && !ALL_DIGITS_PATTERN
                            .matcher(displayValue)
                            .matches()
                    && !isNonEntityStatusPhrase(displayValue)
                    && displayValue.length() >= 2;

            case PERSON ->
                    !CURRENCY_CODES.contains(normalized)
                    && !MASKED_ACCOUNT_PATTERN
                            .matcher(displayValue)
                            .matches()
                    && !ALL_DIGITS_PATTERN
                            .matcher(displayValue)
                            .matches()
                    && !looksLikeOrganization(displayValue)
                    && !isNonEntityStatusPhrase(displayValue);

            case PHONE_NUMBER ->
                    isPlausiblePhone(displayValue);

            case MONEY ->
                    isPlausibleMoney(displayValue);

            case EMAIL_ADDRESS ->
                    displayValue.contains("@");

            case URL ->
                    normalized.startsWith("http://")
                    || normalized.startsWith("https://");

            case IP_ADDRESS ->
                    displayValue.contains(".");

            case DATE,
                    TIME,
                    DATE_TIME ->
                    true;
        };
    }

    private boolean isPlausibleMoney(
            String value
    ) {

        if (
                MASKED_ACCOUNT_PATTERN
                        .matcher(value)
                        .matches()
        ) {
            return false;
        }

        return MONEY_VALUE_PATTERN
                .matcher(value)
                .matches();
    }

    private boolean isPlausiblePhone(
            String value
    ) {

        String compact =
                value.replaceAll("\\s+", "");

        if (
                COMPACT_TIMESTAMP_PATTERN
                        .matcher(compact)
                        .matches()
        ) {
            return false;
        }

        String digits =
                value.replaceAll("\\D", "");

        if (
                digits.length() < 8
                || digits.length() > 15
        ) {
            return false;
        }

        if (
                digits.length() == 14
                && (
                        digits.startsWith("19")
                        || digits.startsWith("20")
                )
        ) {
            return false;
        }

        return true;
    }

    private List<IntelligenceTimelineEventContent>
            normalizeTimelineEvents(

                    List<IntelligenceTimelineEventContent> values,
                    String evidenceText
            ) {

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        if (
                values.size()
                > properties.getMaxTimelineEvents()
        ) {
            throw new AiResponseValidationException(
                    "The intelligence response contains "
                    + "too many timeline events"
            );
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(value -> normalizeTimelineEvent(
                        value,
                        evidenceText
                ))
                .filter(Objects::nonNull)
                .toList();
    }

    private IntelligenceTimelineEventContent
            normalizeTimelineEvent(

                    IntelligenceTimelineEventContent value,
                    String evidenceText
            ) {

        String title =
                requireText(
                        value.title(),
                        "A timeline event is missing its title",
                        properties.getMaxTitleCharacters()
                );

        String description =
                requireText(
                        value.description(),
                        "A timeline event is missing "
                        + "its description",
                        properties.getMaxDescriptionCharacters()
                );

        String contextSnippet =
                requireText(
                        value.contextSnippet(),
                        "A timeline event is missing "
                        + "its evidence context",
                        properties.getMaxContextCharacters()
                );

        requireEvidenceSupport(
                evidenceText,
                contextSnippet,
                "A timeline-event context is not "
                + "supported by the evidence"
        );

        TimelineTemporalPrecision precision =
                value.temporalPrecision() == null
                        ? TimelineTemporalPrecision.UNKNOWN
                        : value.temporalPrecision();

        TimelineEventCertainty certainty =
                value.certainty() == null
                        ? TimelineEventCertainty.UNKNOWN
                        : value.certainty();

        String temporalExpression =
                normalizeOptionalText(
                        value.temporalExpression(),
                        properties.getMaxValueCharacters()
                );

        String normalizedDateTime =
                normalizeOptionalText(
                        value.normalizedDateTime(),
                        50
                );

        validateDateTime(
                normalizedDateTime,
                precision
        );

        /*
         * Static review/status flags are not timeline events,
         * even when the AI incorrectly attaches a timestamp.
         */
        if (isStaticFlagTitle(title)) {
            return null;
        }

        List<IntelligenceEntityReferenceContent>
                involvedEntities =
                        normalizeReferences(
                                value.involvedEntities(),
                                evidenceText
                        );

        return new IntelligenceTimelineEventContent(
                title,
                description,
                temporalExpression,
                normalizedDateTime,
                precision,
                certainty,
                contextSnippet,
                involvedEntities
        );
    }

    private boolean isStaticFlagTitle(
            String title
    ) {

        String normalizedTitle =
                title
                        .strip()
                        .toLowerCase(Locale.ROOT);

        return normalizedTitle.endsWith(" flag")
                || normalizedTitle.contains("review flag")
                || normalizedTitle.contains("status flag");
    }

    private List<IntelligenceEntityReferenceContent>
            normalizeReferences(

                    List<IntelligenceEntityReferenceContent> values,
                    String evidenceText
            ) {

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        if (
                values.size()
                > properties.getMaxEventEntityLinks()
        ) {
            throw new AiResponseValidationException(
                    "A timeline event contains too many "
                    + "entity references"
            );
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(value -> {

                    if (value.entityType() == null) {
                        throw new AiResponseValidationException(
                                "A timeline entity reference "
                                + "is missing its type"
                        );
                    }

                    String referenceValue =
                            requireText(
                                    value.value(),
                                    "A timeline entity reference "
                                    + "is missing its value",
                                    properties
                                            .getMaxValueCharacters()
                            );

                    requireEvidenceSupport(
                            evidenceText,
                            referenceValue,
                            "A timeline entity reference is "
                            + "not supported by the evidence"
                    );

                    ExtractedEntityType correctedType =
                            correctEntityType(
                                    value.entityType(),
                                    referenceValue
                            );

                    if (
                            !isSemanticallyPlausible(
                                    correctedType,
                                    referenceValue
                            )
                    ) {
                        return null;
                    }

                    return new
                            IntelligenceEntityReferenceContent(
                                    correctedType,
                                    referenceValue
                            );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private void validateDateTime(
            String normalizedDateTime,
            TimelineTemporalPrecision precision
    ) {

        if (normalizedDateTime.isBlank()) {

            if (
                    precision
                    == TimelineTemporalPrecision.DATE_TIME
            ) {
                throw new AiResponseValidationException(
                        "A DATE_TIME timeline event must "
                        + "contain normalizedDateTime"
                );
            }

            return;
        }

        try {
            LocalDateTime.parse(normalizedDateTime);
        }
        catch (DateTimeParseException exception) {
            throw new AiResponseValidationException(
                    "Timeline normalizedDateTime must use "
                    + "ISO local date-time format"
            );
        }
    }

    private String requireText(
            String value,
            String errorMessage,
            int maximumLength
    ) {

        String normalized =
                normalizationService
                        .normalizeDisplayValue(value);

        if (normalized.isBlank()) {
            throw new AiResponseValidationException(
                    errorMessage
            );
        }

        if (normalized.length() > maximumLength) {
            throw new AiResponseValidationException(
                    errorMessage
                    + " or exceeds its allowed length"
            );
        }

        return normalized;
    }

    private String normalizeOptionalText(
            String value,
            int maximumLength
    ) {

        String normalized =
                normalizationService
                        .normalizeDisplayValue(value);

        if (normalized.length() > maximumLength) {
            throw new AiResponseValidationException(
                    "An intelligence response value "
                    + "exceeds its allowed length"
            );
        }

        return normalized;
    }

    private void requireEvidenceSupport(
            String evidenceText,
            String candidate,
            String errorMessage
    ) {

        String normalizedEvidence =
                normalizeForComparison(evidenceText);

        String normalizedCandidate =
                normalizeForComparison(candidate);

        if (
                !normalizedEvidence.contains(
                        normalizedCandidate
                )
        ) {
            throw new AiResponseValidationException(
                    errorMessage
            );
        }
    }

    private String normalizeForComparison(
            String value
    ) {

        return normalizationService
                .normalizeDisplayValue(value)
                .toLowerCase(Locale.ROOT);
    }
}