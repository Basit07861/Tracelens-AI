package com.tracelens.evidence.service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tracelens.evidence.config.EvidenceProperties;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.exception.InvalidEvidenceFileException;

@Component
public class EvidenceFileValidator {

    private static final int MAXIMUM_ORIGINAL_FILE_NAME_LENGTH =
            255;

    private static final int MAXIMUM_DESCRIPTION_LENGTH =
            500;

    private static final Map<EvidenceFileType, Set<String>>
            ALLOWED_CONTENT_TYPES = Map.of(

                    EvidenceFileType.PDF,
                    Set.of(
                            MediaType.APPLICATION_PDF_VALUE,
                            MediaType.APPLICATION_OCTET_STREAM_VALUE
                    ),

                    EvidenceFileType.TXT,
                    Set.of(
                            MediaType.TEXT_PLAIN_VALUE,
                            MediaType.APPLICATION_OCTET_STREAM_VALUE
                    ),

                    EvidenceFileType.CSV,
                    Set.of(
                            "text/csv",
                            "application/csv",
                            "application/vnd.ms-excel",
                            MediaType.TEXT_PLAIN_VALUE,
                            MediaType.APPLICATION_OCTET_STREAM_VALUE
                    ),

                    EvidenceFileType.JSON,
                    Set.of(
                            MediaType.APPLICATION_JSON_VALUE,
                            "text/json",
                            MediaType.TEXT_PLAIN_VALUE,
                            MediaType.APPLICATION_OCTET_STREAM_VALUE
                    )
            );

    private final EvidenceProperties evidenceProperties;

    public EvidenceFileValidator(
            EvidenceProperties evidenceProperties
    ) {
        this.evidenceProperties = evidenceProperties;
    }

    public EvidenceFileType validateAndResolveFileType(
            MultipartFile file
    ) {

        validatePresenceAndSize(file);

        String originalFileName =
                sanitizeOriginalFileName(
                        file.getOriginalFilename()
                );

        String extension =
                extractExtension(originalFileName);

        EvidenceFileType fileType =
                resolveFileType(extension);

        String contentType =
                normalizeContentType(file.getContentType());

        validateContentType(fileType, contentType);

        return fileType;
    }

    public String sanitizeOriginalFileName(
            String originalFileName
    ) {

        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new InvalidEvidenceFileException(
                    "Evidence filename is required"
            );
        }

        String normalizedName = originalFileName
                .replace('\\', '/');

        int lastSeparatorIndex =
                normalizedName.lastIndexOf('/');

        if (lastSeparatorIndex >= 0) {
            normalizedName = normalizedName.substring(
                    lastSeparatorIndex + 1
            );
        }

        normalizedName = normalizedName
                .replaceAll("[\\p{Cntrl}]", "")
                .trim();

        if (normalizedName.isBlank()
                || ".".equals(normalizedName)
                || "..".equals(normalizedName)) {

            throw new InvalidEvidenceFileException(
                    "Evidence filename is invalid"
            );
        }

        if (normalizedName.length()
                > MAXIMUM_ORIGINAL_FILE_NAME_LENGTH) {

            throw new InvalidEvidenceFileException(
                    "Evidence filename cannot exceed "
                    + MAXIMUM_ORIGINAL_FILE_NAME_LENGTH
                    + " characters"
            );
        }

        return normalizedName;
    }

    public String normalizeContentType(
            String contentType
    ) {

        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        int parameterSeparatorIndex =
                contentType.indexOf(';');

        String normalizedContentType =
                parameterSeparatorIndex >= 0
                        ? contentType.substring(
                                0,
                                parameterSeparatorIndex
                        )
                        : contentType;

        return normalizedContentType
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public String normalizeDescription(
            String description
    ) {

        if (description == null || description.isBlank()) {
            return null;
        }

        String normalizedDescription =
                description.trim();

        if (normalizedDescription.length()
                > MAXIMUM_DESCRIPTION_LENGTH) {

            throw new InvalidEvidenceFileException(
                    "Evidence description cannot exceed "
                    + MAXIMUM_DESCRIPTION_LENGTH
                    + " characters"
            );
        }

        return normalizedDescription;
    }

    private void validatePresenceAndSize(
            MultipartFile file
    ) {

        if (file == null) {
            throw new InvalidEvidenceFileException(
                    "Evidence file is required"
            );
        }

        if (file.isEmpty() || file.getSize() <= 0) {
            throw new InvalidEvidenceFileException(
                    "Evidence file cannot be empty"
            );
        }

        if (file.getSize()
                > evidenceProperties.getMaxFileSizeBytes()) {

            throw new InvalidEvidenceFileException(
                    "Evidence file cannot exceed 10 MB"
            );
        }
    }

    private String extractExtension(
            String fileName
    ) {

        int finalDotIndex =
                fileName.lastIndexOf('.');

        if (finalDotIndex <= 0
                || finalDotIndex
                == fileName.length() - 1) {

            throw new InvalidEvidenceFileException(
                    "Evidence file must have a supported extension"
            );
        }

        return fileName
                .substring(finalDotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    private EvidenceFileType resolveFileType(
            String extension
    ) {

        return switch (extension) {

            case "pdf" -> EvidenceFileType.PDF;
            case "txt" -> EvidenceFileType.TXT;
            case "csv" -> EvidenceFileType.CSV;
            case "json" -> EvidenceFileType.JSON;

            default -> throw new InvalidEvidenceFileException(
                    "Unsupported evidence file type. "
                    + "Supported types are PDF, TXT, CSV and JSON"
            );
        };
    }

    private void validateContentType(
            EvidenceFileType fileType,
            String contentType
    ) {

        Set<String> allowedContentTypes =
                ALLOWED_CONTENT_TYPES.get(fileType);

        if (!allowedContentTypes.contains(contentType)) {

            throw new InvalidEvidenceFileException(
                    "The uploaded file content type does not "
                    + "match its extension"
            );
        }
    }
}