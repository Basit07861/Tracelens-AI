package com.tracelens.evidence.extraction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;

import com.tracelens.exception.EvidenceTextExtractionException;

public final class TextExtractionSupport {

    private static final char UTF_8_BOM = '\uFEFF';

    private TextExtractionSupport() {
    }

    public static String readStrictUtf8(
            Resource resource
    ) {

        if (resource == null) {
            throw new EvidenceTextExtractionException(
                    "Evidence resource is unavailable"
            );
        }

        try (
                InputStream inputStream =
                        resource.getInputStream()
        ) {

            byte[] bytes =
                    inputStream.readAllBytes();

            String decodedText =
                    StandardCharsets.UTF_8
                            .newDecoder()
                            .onMalformedInput(
                                    CodingErrorAction.REPORT
                            )
                            .onUnmappableCharacter(
                                    CodingErrorAction.REPORT
                            )
                            .decode(
                                    ByteBuffer.wrap(bytes)
                            )
                            .toString();

            return removeUtf8Bom(decodedText);
        }
        catch (CharacterCodingException exception) {
            throw new EvidenceTextExtractionException(
                    "Evidence text is not valid UTF-8",
                    exception
            );
        }
        catch (IOException exception) {
            throw new EvidenceTextExtractionException(
                    "Evidence content could not be read",
                    exception
            );
        }
    }

    public static String normalizeLineEndings(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    public static String requireNonBlank(
            String text,
            String message
    ) {

        if (text == null || text.isBlank()) {
            throw new EvidenceTextExtractionException(
                    message
            );
        }

        return text;
    }

    public static String enforceCharacterLimit(
            String text,
            int maximumCharacters
    ) {

        if (text == null) {
            return "";
        }

        if (text.length() > maximumCharacters) {
            throw new EvidenceTextExtractionException(
                    "Extracted evidence text exceeds "
                    + "the configured limit of "
                    + maximumCharacters
                    + " characters"
            );
        }

        return text;
    }

    public static void appendWithLimit(
            StringBuilder output,
            String value,
            int maximumCharacters
    ) {

        String safeValue =
                value == null ? "" : value;

        if (
                output.length()
                + safeValue.length()
                > maximumCharacters
        ) {
            throw new EvidenceTextExtractionException(
                    "Extracted evidence text exceeds "
                    + "the configured limit of "
                    + maximumCharacters
                    + " characters"
            );
        }

        output.append(safeValue);
    }

    public static String normalizeInlineValue(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return normalizeLineEndings(value)
                .replace("\n", " | ")
                .replace("\t", " ")
                .strip();
    }

    public static void rejectLikelyBinaryText(
            String text
    ) {

        if (text.indexOf('\0') >= 0) {
            throw new EvidenceTextExtractionException(
                    "The TXT evidence appears to contain "
                    + "binary content"
            );
        }

        int suspiciousControlCharacters = 0;

        for (int index = 0;
                index < text.length();
                index++) {

            char currentCharacter =
                    text.charAt(index);

            boolean allowedWhitespace =
                    currentCharacter == '\n'
                    || currentCharacter == '\r'
                    || currentCharacter == '\t'
                    || currentCharacter == '\f';

            if (
                    Character.isISOControl(
                            currentCharacter
                    )
                    && !allowedWhitespace
            ) {
                suspiciousControlCharacters++;
            }
        }

        int permittedControlCharacters =
                Math.max(
                        3,
                        text.length() / 200
                );

        if (
                suspiciousControlCharacters
                > permittedControlCharacters
        ) {
            throw new EvidenceTextExtractionException(
                    "The TXT evidence appears to contain "
                    + "unsupported binary or control data"
            );
        }
    }

    private static String removeUtf8Bom(
            String text
    ) {

        if (
                text != null
                && !text.isEmpty()
                && text.charAt(0) == UTF_8_BOM
        ) {
            return text.substring(1);
        }

        return text;
    }
}