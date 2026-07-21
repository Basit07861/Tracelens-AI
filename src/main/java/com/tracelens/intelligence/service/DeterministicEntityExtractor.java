package com.tracelens.intelligence.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.tracelens.intelligence.config.IntelligenceExtractionProperties;
import com.tracelens.intelligence.entity.ExtractedEntityType;

@Service
public class DeterministicEntityExtractor {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "(?i)(?<![A-Z0-9._%+-])"
                    + "([A-Z0-9._%+-]+"
                    + "@[A-Z0-9.-]+\\.[A-Z]{2,})"
                    + "(?![A-Z0-9._%+-])"
            );

    private static final Pattern URL_PATTERN =
            Pattern.compile(
                    "(?i)\\bhttps?://[^\\s<>\"']+"
            );

    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"
            );

    private static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "(?<!\\w)"
                    + "(?:\\+?\\d[\\d\\s().-]{6,}\\d)"
                    + "(?!\\w)"
            );

    private static final Pattern MONEY_PATTERN =
            Pattern.compile(
                    "(?i)(?<!\\w)"
                    + "(?:₹|Rs\\.?|INR|USD|EUR|GBP|"
                    + "\\$|€|£)"
                    + "\\s*\\d[\\d,]*"
                    + "(?:\\.\\d{1,2})?"
                    + "(?!\\w)"
            );

    private static final Pattern ISO_DATE_PATTERN =
            Pattern.compile(
                    "\\b\\d{4}-\\d{2}-\\d{2}\\b"
            );

    private static final Pattern NUMERIC_DATE_PATTERN =
            Pattern.compile(
                    "\\b\\d{1,2}[/-]\\d{1,2}"
                    + "[/-]\\d{2,4}\\b"
            );

    private static final Pattern NAMED_DATE_PATTERN =
            Pattern.compile(
                    "(?i)\\b\\d{1,2}\\s+"
                    + "(?:Jan(?:uary)?|Feb(?:ruary)?|"
                    + "Mar(?:ch)?|Apr(?:il)?|May|"
                    + "Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|"
                    + "Sep(?:tember)?|Oct(?:ober)?|"
                    + "Nov(?:ember)?|Dec(?:ember)?)"
                    + "\\s+\\d{4}\\b"
            );

    private static final Pattern TIME_PATTERN =
            Pattern.compile(
                    "(?i)\\b"
                    + "(?:"
                    + "(?:[01]?\\d|2[0-3]):[0-5]\\d"
                    + "|"
                    + "(?:1[0-2]|0?[1-9])"
                    + "(?::[0-5]\\d)?\\s?[AP]M"
                    + ")"
                    + "\\b"
            );

    /*
     * Examples:
     *
     * 20260718-170241
     * 20260718170241
     *
     * These are compact date-time values, not phone numbers.
     */
    private static final Pattern
            COMPACT_TIMESTAMP_PATTERN =
                    Pattern.compile(
                            "^\\d{8}[-_]?\\d{6}$"
                    );

    private final IntelligenceExtractionProperties
            properties;

    private final IntelligenceEntityNormalizationService
            normalizationService;

    public DeterministicEntityExtractor(
            IntelligenceExtractionProperties properties,
            IntelligenceEntityNormalizationService
                    normalizationService
    ) {
        this.properties = properties;
        this.normalizationService =
                normalizationService;
    }

    public List<IntelligenceEntityCandidate> extract(
            String evidenceText
    ) {

        Map<String, MutableCandidate> candidates =
                new LinkedHashMap<>();

        extractPattern(
                evidenceText,
                EMAIL_PATTERN,
                ExtractedEntityType.EMAIL_ADDRESS,
                value -> true,
                candidates
        );

        extractPattern(
                evidenceText,
                URL_PATTERN,
                ExtractedEntityType.URL,
                value -> true,
                candidates
        );

        extractPattern(
                evidenceText,
                IPV4_PATTERN,
                ExtractedEntityType.IP_ADDRESS,
                this::isValidIpv4,
                candidates
        );

        extractPattern(
                evidenceText,
                MONEY_PATTERN,
                ExtractedEntityType.MONEY,
                value -> true,
                candidates
        );

        extractPattern(
                evidenceText,
                ISO_DATE_PATTERN,
                ExtractedEntityType.DATE,
                value -> true,
                candidates
        );

        extractPattern(
                evidenceText,
                NUMERIC_DATE_PATTERN,
                ExtractedEntityType.DATE,
                value -> true,
                candidates
        );

        extractPattern(
                evidenceText,
                NAMED_DATE_PATTERN,
                ExtractedEntityType.DATE,
                value -> true,
                candidates
        );

        extractPattern(
                evidenceText,
                TIME_PATTERN,
                ExtractedEntityType.TIME,
                value -> true,
                candidates
        );

        extractPattern(
                evidenceText,
                PHONE_PATTERN,
                ExtractedEntityType.PHONE_NUMBER,
                this::isPossiblePhone,
                candidates
        );

        return candidates
                .values()
                .stream()
                .map(MutableCandidate::toImmutable)
                .toList();
    }

    private void extractPattern(
            String evidenceText,
            Pattern pattern,
            ExtractedEntityType type,
            Predicate<String> validator,
            Map<String, MutableCandidate> candidates
    ) {

        Matcher matcher = pattern.matcher(evidenceText);

        while (matcher.find()) {

            String value = cleanMatchedValue(
                    type,
                    matcher.group()
            );

            if (value.isBlank()
                    || !validator.test(value)) {

                continue;
            }

            String normalizedValue =
                    normalizationService.normalizeValue(
                            type,
                            value
                    );

            if (normalizedValue.isBlank()) {
                continue;
            }

            String key =
                    normalizationService.createKey(
                            type,
                            normalizedValue
                    );

            MutableCandidate existing =
                    candidates.get(key);

            if (existing == null) {
                candidates.put(
                        key,
                        new MutableCandidate(
                                type,
                                value,
                                normalizedValue,
                                createContextSnippet(
                                        evidenceText,
                                        matcher.start(),
                                        matcher.end()
                                ),
                                BigDecimal.ONE,
                                1,
                                matcher.start(),
                                matcher.end()
                        )
                );
            }
            else {
                existing.incrementOccurrenceCount();
            }
        }
    }

    private String cleanMatchedValue(
            ExtractedEntityType type,
            String value
    ) {

        String cleaned =
                normalizationService
                        .normalizeDisplayValue(value);

        if (type == ExtractedEntityType.URL) {
            cleaned = cleaned.replaceAll(
                    "[.,;:!?)}\\]]+$",
                    ""
            );
        }

        return cleaned;
    }

    private boolean isValidIpv4(
            String value
    ) {

        String[] parts = value.split("\\.");

        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int number = Integer.parseInt(part);

                if (number < 0 || number > 255) {
                    return false;
                }
            }
            catch (NumberFormatException exception) {
                return false;
            }
        }

        return true;
    }

    private boolean isPossiblePhone(
            String value
    ) {

        String compactValue =
                value.replaceAll("\\s+", "");

        /*
         * Prevent transaction timestamps such as
         * 20260718-170241 from being classified as phones.
         */
        if (
                COMPACT_TIMESTAMP_PATTERN
                        .matcher(compactValue)
                        .matches()
        ) {
            return false;
        }

        if (
                ISO_DATE_PATTERN.matcher(value).matches()
                || NUMERIC_DATE_PATTERN
                        .matcher(value)
                        .matches()
        ) {
            return false;
        }

        String digits = value.replaceAll(
                "\\D",
                ""
        );

        if (digits.length() < 8
                || digits.length() > 15) {

            return false;
        }

        /*
         * A 14-digit value beginning with a modern year
         * is likely yyyyMMddHHmmss rather than a phone.
         */
        if (
                digits.length() == 14
                && (
                        digits.startsWith("19")
                        || digits.startsWith("20")
                )
        ) {
            return false;
        }

        /*
         * Plain phone numbers without formatting are accepted
         * only when they contain ten digits.
         */
        boolean hasPhoneFormatting =
                value.startsWith("+")
                || value.contains(" ")
                || value.contains("(")
                || value.contains(")")
                || value.contains(".");

        if (!hasPhoneFormatting
                && !value.contains("-")
                && digits.length() != 10) {

            return false;
        }

        return true;
    }

    private String createContextSnippet(
            String text,
            int start,
            int end
    ) {

        int maximumLength =
                properties.getMaxContextCharacters();

        int availableAround =
                Math.max(
                        maximumLength
                        - (end - start),
                        0
                );

        int before = availableAround / 2;
        int after = availableAround - before;

        int snippetStart = Math.max(
                0,
                start - before
        );

        int snippetEnd = Math.min(
                text.length(),
                end + after
        );

        return normalizationService
                .normalizeDisplayValue(
                        text.substring(
                                snippetStart,
                                snippetEnd
                        )
                );
    }

    private static final class MutableCandidate {

        private final ExtractedEntityType type;
        private final String displayValue;
        private final String normalizedValue;
        private final String contextSnippet;
        private final BigDecimal confidence;

        private int occurrenceCount;

        private final Integer firstOffset;
        private final Integer lastOffset;

        private MutableCandidate(
                ExtractedEntityType type,
                String displayValue,
                String normalizedValue,
                String contextSnippet,
                BigDecimal confidence,
                int occurrenceCount,
                Integer firstOffset,
                Integer lastOffset
        ) {
            this.type = type;
            this.displayValue = displayValue;
            this.normalizedValue = normalizedValue;
            this.contextSnippet = contextSnippet;
            this.confidence = confidence;
            this.occurrenceCount = occurrenceCount;
            this.firstOffset = firstOffset;
            this.lastOffset = lastOffset;
        }

        private void incrementOccurrenceCount() {
            occurrenceCount++;
        }

        private IntelligenceEntityCandidate
                toImmutable() {

            return new IntelligenceEntityCandidate(
                    type,
                    displayValue,
                    normalizedValue,
                    contextSnippet,
                    confidence,
                    occurrenceCount,
                    firstOffset,
                    lastOffset
            );
        }
    }
}