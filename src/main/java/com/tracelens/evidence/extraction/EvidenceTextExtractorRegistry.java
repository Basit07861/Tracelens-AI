package com.tracelens.evidence.extraction;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.exception.EvidenceTextExtractionException;

@Component
public class EvidenceTextExtractorRegistry {

    private final Map<
            EvidenceFileType,
            EvidenceTextExtractor
            > extractors;

    public EvidenceTextExtractorRegistry(
            List<EvidenceTextExtractor> extractorList
    ) {

        EnumMap<
                EvidenceFileType,
                EvidenceTextExtractor
                > registeredExtractors =
                        new EnumMap<>(
                                EvidenceFileType.class
                        );

        for (
                EvidenceTextExtractor extractor
                : extractorList
        ) {

            EvidenceFileType supportedType =
                    extractor.supportedFileType();

            EvidenceTextExtractor existingExtractor =
                    registeredExtractors.putIfAbsent(
                            supportedType,
                            extractor
                    );

            if (existingExtractor != null) {
                throw new IllegalStateException(
                        "More than one evidence text "
                        + "extractor supports "
                        + supportedType
                );
            }
        }

        this.extractors = Collections.unmodifiableMap(
                registeredExtractors
        );
    }

    public EvidenceTextExtractor getExtractor(
            EvidenceFileType fileType
    ) {

        if (fileType == null) {
            throw new EvidenceTextExtractionException(
                    "Evidence file type is unavailable"
            );
        }

        EvidenceTextExtractor extractor =
                extractors.get(fileType);

        if (extractor == null) {
            throw new EvidenceTextExtractionException(
                    "Text extraction is not currently "
                    + "available for "
                    + fileType
                    + " evidence"
            );
        }

        return extractor;
    }

    public boolean supports(
            EvidenceFileType fileType
    ) {
        return fileType != null
                && extractors.containsKey(fileType);
    }
}