package com.tracelens.evidence.extraction;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.tracelens.evidence.config.EvidenceExtractionProperties;
import com.tracelens.evidence.entity.EvidenceFileType;

@Component
public class TxtEvidenceTextExtractor
        implements EvidenceTextExtractor {

    private final EvidenceExtractionProperties properties;

    public TxtEvidenceTextExtractor(
            EvidenceExtractionProperties properties
    ) {
        this.properties = properties;
    }

    @Override
    public EvidenceFileType supportedFileType() {
        return EvidenceFileType.TXT;
    }

    @Override
    public String extract(
            Resource resource
    ) {

        String text =
                TextExtractionSupport.readStrictUtf8(
                        resource
                );

        TextExtractionSupport.rejectLikelyBinaryText(
                text
        );

        String normalizedText =
                TextExtractionSupport
                        .normalizeLineEndings(text)
                        .strip();

        TextExtractionSupport.requireNonBlank(
                normalizedText,
                "The TXT evidence does not contain "
                + "extractable text"
        );

        return TextExtractionSupport
                .enforceCharacterLimit(
                        normalizedText,
                        properties.getMaxCharacters()
                );
    }
}