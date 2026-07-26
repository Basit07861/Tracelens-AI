package com.tracelens.evidence.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import com.tracelens.evidence.config.EvidenceProperties;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.exception.EvidenceStorageException;

class EvidenceStorageServiceTest {

    @TempDir
    Path temporaryDirectory;

    private Path storageRoot;

    private EvidenceStorageService storageService;

    @BeforeEach
    void setUp() {

        storageRoot =
                temporaryDirectory.resolve(
                        "evidence-storage"
                );

        EvidenceProperties evidenceProperties =
                new EvidenceProperties();

        evidenceProperties.setStorageRoot(
                storageRoot.toString()
        );

        storageService =
                new EvidenceStorageService(
                        evidenceProperties
                );

        storageService.initializeStorage();
    }

    @Test
    void storesHashesLoadsAndDeletesEvidence()
            throws Exception {

        byte[] content =
                "TraceLens evidence file"
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "invoice.txt",
                        "text/plain",
                        content
                );

        StoredEvidenceFile storedFile =
                storageService.store(
                        7L,
                        file,
                        EvidenceFileType.TXT
                );

        assertTrue(
                storedFile.relativePath()
                        .startsWith("case-7/")
        );

        assertTrue(
                storedFile.storedFileName()
                        .endsWith(".txt")
        );

        assertEquals(
                content.length,
                storedFile.fileSizeBytes()
        );

        assertEquals(
                calculateExpectedSha256(content),
                storedFile.sha256Hash()
        );

        Path physicalFile =
                storageRoot.resolve(
                        storedFile.relativePath()
                );

        assertTrue(Files.exists(physicalFile));

        assertArrayEquals(
                content,
                Files.readAllBytes(physicalFile)
        );

        assertEquals(
                storedFile.sha256Hash(),
                storageService.calculateSha256(
                        storedFile.relativePath()
                )
        );

        Resource resource =
                storageService.loadAsResource(
                        storedFile.relativePath()
                );

        try (
                InputStream inputStream =
                        resource.getInputStream()
        ) {

            assertArrayEquals(
                    content,
                    inputStream.readAllBytes()
            );
        }

        storageService.delete(
                storedFile.relativePath()
        );

        assertFalse(Files.exists(physicalFile));
    }

    @Test
    void identicalBytesProduceSameSha256InDifferentCases() {

        byte[] content =
                "Repeated forensic evidence"
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        MockMultipartFile firstFile =
                new MockMultipartFile(
                        "file",
                        "first.txt",
                        "text/plain",
                        content
                );

        MockMultipartFile secondFile =
                new MockMultipartFile(
                        "file",
                        "second.txt",
                        "text/plain",
                        content
                );

        StoredEvidenceFile firstStoredFile =
                storageService.store(
                        1L,
                        firstFile,
                        EvidenceFileType.TXT
                );

        StoredEvidenceFile secondStoredFile =
                storageService.store(
                        2L,
                        secondFile,
                        EvidenceFileType.TXT
                );

        assertEquals(
                firstStoredFile.sha256Hash(),
                secondStoredFile.sha256Hash()
        );

        assertNotEquals(
                firstStoredFile.relativePath(),
                secondStoredFile.relativePath()
        );
    }

    @Test
    void rejectsPathTraversalOutsideStorageRoot() {

        EvidenceStorageException exception =
                assertThrows(
                        EvidenceStorageException.class,
                        () -> storageService
                                .calculateSha256(
                                        "../outside.txt"
                                )
                );

        assertEquals(
                "Evidence storage path is invalid",
                exception.getMessage()
        );
    }

    @Test
    void rejectsInvalidCaseIdentifier() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "evidence.txt",
                        "text/plain",
                        "content".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        EvidenceStorageException exception =
                assertThrows(
                        EvidenceStorageException.class,
                        () -> storageService.store(
                                0L,
                                file,
                                EvidenceFileType.TXT
                        )
                );

        assertEquals(
                "Cannot store evidence for an invalid case",
                exception.getMessage()
        );
    }

    private String calculateExpectedSha256(
            byte[] content
    ) throws Exception {

        MessageDigest messageDigest =
                MessageDigest.getInstance("SHA-256");

        return HexFormat
                .of()
                .formatHex(
                        messageDigest.digest(content)
                );
    }
}