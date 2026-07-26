package com.tracelens.evidence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.tracelens.evidence.config.EvidenceProperties;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.exception.InvalidEvidenceFileException;

class EvidenceFileValidatorTest {

    private EvidenceProperties evidenceProperties;

    private EvidenceFileValidator validator;

    @BeforeEach
    void setUp() {

        evidenceProperties =
                new EvidenceProperties();

        evidenceProperties.setMaxFileSizeBytes(
                10L * 1024L * 1024L
        );

        validator =
                new EvidenceFileValidator(
                        evidenceProperties
                );
    }

    @Test
    void validatesSupportedTxtFileAndSanitizesFilename() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "C:\\private\\evidence\\Invoice.TXT",
                        "text/plain; charset=UTF-8",
                        "Invoice evidence content"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        EvidenceFileType fileType =
                validator.validateAndResolveFileType(
                        file
                );

        assertEquals(
                EvidenceFileType.TXT,
                fileType
        );

        assertEquals(
                "Invoice.TXT",
                validator.sanitizeOriginalFileName(
                        file.getOriginalFilename()
                )
        );

        assertEquals(
                "text/plain",
                validator.normalizeContentType(
                        file.getContentType()
                )
        );

        assertEquals(
                "Manual investigator description",
                validator.normalizeDescription(
                        "  Manual investigator description  "
                )
        );

        assertNull(
                validator.normalizeDescription("   ")
        );
    }

    @Test
    void rejectsEmptyEvidenceFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "empty.txt",
                        "text/plain",
                        new byte[0]
                );

        InvalidEvidenceFileException exception =
                assertThrows(
                        InvalidEvidenceFileException.class,
                        () -> validator
                                .validateAndResolveFileType(file)
                );

        assertEquals(
                "Evidence file cannot be empty",
                exception.getMessage()
        );
    }

    @Test
    void rejectsEvidenceLargerThanConfiguredLimit() {

        evidenceProperties.setMaxFileSizeBytes(5L);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "large.txt",
                        "text/plain",
                        new byte[6]
                );

        assertThrows(
                InvalidEvidenceFileException.class,
                () -> validator
                        .validateAndResolveFileType(file)
        );
    }

    @Test
    void rejectsUnsupportedFileExtension() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "malware.exe",
                        "application/octet-stream",
                        new byte[] {
                                1,
                                2,
                                3
                        }
                );

        InvalidEvidenceFileException exception =
                assertThrows(
                        InvalidEvidenceFileException.class,
                        () -> validator
                                .validateAndResolveFileType(file)
                );

        assertEquals(
                "Unsupported evidence file type. "
                        + "Supported types are PDF, TXT, CSV and JSON",
                exception.getMessage()
        );
    }

    @Test
    void rejectsContentTypeThatDoesNotMatchExtension() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.pdf",
                        "text/plain",
                        "This is not declared as PDF"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        InvalidEvidenceFileException exception =
                assertThrows(
                        InvalidEvidenceFileException.class,
                        () -> validator
                                .validateAndResolveFileType(file)
                );

        assertEquals(
                "The uploaded file content type does not "
                        + "match its extension",
                exception.getMessage()
        );
    }

    @Test
    void rejectsDescriptionOverFiveHundredCharacters() {

        String oversizedDescription =
                "x".repeat(501);

        InvalidEvidenceFileException exception =
                assertThrows(
                        InvalidEvidenceFileException.class,
                        () -> validator.normalizeDescription(
                                oversizedDescription
                        )
                );

        assertEquals(
                "Evidence description cannot exceed "
                        + "500 characters",
                exception.getMessage()
        );
    }
}