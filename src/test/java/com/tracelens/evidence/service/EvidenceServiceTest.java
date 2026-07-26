package com.tracelens.evidence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.tracelens.evidence.dto.EvidenceIntegrityResponse;
import com.tracelens.evidence.dto.EvidenceResponse;
import com.tracelens.evidence.entity.Evidence;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.evidence.entity.EvidenceIntegrityStatus;
import com.tracelens.evidence.entity.EvidenceStatus;
import com.tracelens.evidence.repository.EvidenceRepository;
import com.tracelens.evidence.storage.EvidenceStorageService;
import com.tracelens.evidence.storage.StoredEvidenceFile;
import com.tracelens.exception.DuplicateEvidenceException;
import com.tracelens.exception.EvidenceNotFoundException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.investigation.entity.CasePriority;
import com.tracelens.investigation.entity.CaseStatus;
import com.tracelens.investigation.entity.InvestigationCase;
import com.tracelens.investigation.repository.InvestigationCaseRepository;
import com.tracelens.user.entity.User;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    private static final String OWNER_EMAIL =
            "owner@example.com";

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private InvestigationCaseRepository caseRepository;

    @Mock
    private EvidenceFileValidator fileValidator;

    @Mock
    private EvidenceStorageService storageService;

    private EvidenceService evidenceService;

    @BeforeEach
    void setUp() {

        evidenceService =
                new EvidenceService(
                        evidenceRepository,
                        caseRepository,
                        fileValidator,
                        storageService
                );
    }

    @Test
    void uploadsEvidenceAndStoresVerifiedBaseline() {

        InvestigationCase investigationCase =
                createInvestigationCase();

        MockMultipartFile file =
                createTxtFile();

        String originalHash =
                "a".repeat(64);

        StoredEvidenceFile storedFile =
                new StoredEvidenceFile(
                        "generated-file.txt",
                        "case-9/generated-file.txt",
                        file.getSize(),
                        originalHash
                );

        when(
                caseRepository
                        .findByIdAndOwnerEmailIgnoreCase(
                                9L,
                                OWNER_EMAIL
                        )
        ).thenReturn(
                Optional.of(investigationCase)
        );

        when(
                fileValidator
                        .validateAndResolveFileType(file)
        ).thenReturn(EvidenceFileType.TXT);

        when(
                fileValidator
                        .sanitizeOriginalFileName(
                                file.getOriginalFilename()
                        )
        ).thenReturn("invoice.txt");

        when(
                fileValidator.normalizeContentType(
                        file.getContentType()
                )
        ).thenReturn("text/plain");

        when(
                fileValidator.normalizeDescription(
                        " Invoice under review "
                )
        ).thenReturn("Invoice under review");

        when(
                storageService.store(
                        9L,
                        file,
                        EvidenceFileType.TXT
                )
        ).thenReturn(storedFile);

        when(
                evidenceRepository
                        .existsByInvestigationCaseIdAndSha256Hash(
                                9L,
                                originalHash
                        )
        ).thenReturn(false);

        when(
                evidenceRepository.saveAndFlush(
                        any(Evidence.class)
                )
        ).thenAnswer(invocation -> {

            Evidence evidence =
                    invocation.getArgument(0);

            evidence.setId(41L);
            evidence.beforeInsert();

            return evidence;
        });

        EvidenceResponse response =
                evidenceService.uploadEvidence(
                        9L,
                        file,
                        " Invoice under review ",
                        " OWNER@EXAMPLE.COM "
                );

        assertEquals(41L, response.id());
        assertEquals(9L, response.caseId());

        assertEquals(
                "TL-TEST-0001",
                response.caseNumber()
        );

        assertEquals(
                "invoice.txt",
                response.originalFileName()
        );

        assertEquals(
                EvidenceFileType.TXT,
                response.fileType()
        );

        assertEquals(
                EvidenceStatus.UPLOADED,
                response.status()
        );

        assertEquals(
                EvidenceIntegrityStatus.VERIFIED,
                response.integrityStatus()
        );

        assertEquals(
                originalHash,
                response.sha256Hash()
        );

        assertNotNull(
                response.lastIntegrityVerifiedAt()
        );
    }

    @Test
    void duplicateEvidenceIsRejectedAndStoredFileIsRemoved() {

        InvestigationCase investigationCase =
                createInvestigationCase();

        MockMultipartFile file =
                createTxtFile();

        String originalHash =
                "b".repeat(64);

        StoredEvidenceFile storedFile =
                new StoredEvidenceFile(
                        "duplicate.txt",
                        "case-9/duplicate.txt",
                        file.getSize(),
                        originalHash
                );

        when(
                caseRepository
                        .findByIdAndOwnerEmailIgnoreCase(
                                9L,
                                OWNER_EMAIL
                        )
        ).thenReturn(
                Optional.of(investigationCase)
        );

        when(
                fileValidator
                        .validateAndResolveFileType(file)
        ).thenReturn(EvidenceFileType.TXT);

        when(
                fileValidator
                        .sanitizeOriginalFileName(
                                file.getOriginalFilename()
                        )
        ).thenReturn("invoice.txt");

        when(
                fileValidator.normalizeContentType(
                        file.getContentType()
                )
        ).thenReturn("text/plain");

        when(
                fileValidator.normalizeDescription(null)
        ).thenReturn(null);

        when(
                storageService.store(
                        9L,
                        file,
                        EvidenceFileType.TXT
                )
        ).thenReturn(storedFile);

        when(
                evidenceRepository
                        .existsByInvestigationCaseIdAndSha256Hash(
                                9L,
                                originalHash
                        )
        ).thenReturn(true);

        DuplicateEvidenceException exception =
                assertThrows(
                        DuplicateEvidenceException.class,
                        () -> evidenceService.uploadEvidence(
                                9L,
                                file,
                                null,
                                OWNER_EMAIL
                        )
                );

        assertEquals(
                "This evidence file already exists "
                        + "in the selected investigation case",
                exception.getMessage()
        );

        verify(storageService).deleteQuietly(
                "case-9/duplicate.txt"
        );

        verify(
                evidenceRepository,
                never()
        ).saveAndFlush(any(Evidence.class));
    }

    @Test
    void integrityVerificationMarksMatchingEvidenceAsVerified() {

        String expectedHash =
                "c".repeat(64);

        Evidence evidence =
                createEvidence(expectedHash);

        when(
                evidenceRepository
                        .findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                                31L,
                                OWNER_EMAIL
                        )
        ).thenReturn(Optional.of(evidence));

        when(
                storageService.calculateSha256(
                        "case-9/evidence.txt"
                )
        ).thenReturn(expectedHash);

        when(
                evidenceRepository.saveAndFlush(evidence)
        ).thenReturn(evidence);

        EvidenceIntegrityResponse response =
                evidenceService.verifyEvidenceIntegrity(
                        31L,
                        " OWNER@EXAMPLE.COM "
                );

        assertTrue(response.matches());

        assertEquals(
                EvidenceIntegrityStatus.VERIFIED,
                evidence.getIntegrityStatus()
        );

        assertNotNull(
                evidence.getLastIntegrityVerifiedAt()
        );

        assertEquals(
                expectedHash,
                evidence.getSha256Hash()
        );

        verify(storageService).calculateSha256(
                "case-9/evidence.txt"
        );
    }

    @Test
    void integrityVerificationMarksChangedEvidenceAsMismatch() {

        String expectedHash =
                "d".repeat(64);

        String changedHash =
                "e".repeat(64);

        Evidence evidence =
                createEvidence(expectedHash);

        when(
                evidenceRepository
                        .findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                                31L,
                                OWNER_EMAIL
                        )
        ).thenReturn(Optional.of(evidence));

        when(
                storageService.calculateSha256(
                        "case-9/evidence.txt"
                )
        ).thenReturn(changedHash);

        when(
                evidenceRepository.saveAndFlush(evidence)
        ).thenReturn(evidence);

        EvidenceIntegrityResponse response =
                evidenceService.verifyEvidenceIntegrity(
                        31L,
                        OWNER_EMAIL
                );

        assertFalse(response.matches());

        assertEquals(
                EvidenceIntegrityStatus.MISMATCH,
                evidence.getIntegrityStatus()
        );

        /*
         * The original baseline must remain unchanged.
         */
        assertEquals(
                expectedHash,
                evidence.getSha256Hash()
        );
    }

    @Test
    void integrityVerificationRejectsEvidenceWithoutBaselineHash() {

        Evidence evidence =
                createEvidence(null);

        when(
                evidenceRepository
                        .findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                                31L,
                                OWNER_EMAIL
                        )
        ).thenReturn(Optional.of(evidence));

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> evidenceService
                                .verifyEvidenceIntegrity(
                                        31L,
                                        OWNER_EMAIL
                                )
                );

        assertEquals(
                "Evidence does not have an original "
                        + "SHA-256 hash available for verification",
                exception.getMessage()
        );

        verify(
                storageService,
                never()
        ).calculateSha256(
                any(String.class)
        );
    }

    @Test
    void unownedEvidenceReturnsSafeNotFoundResponse() {

        when(
                evidenceRepository
                        .findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                                88L,
                                OWNER_EMAIL
                        )
        ).thenReturn(Optional.empty());

        EvidenceNotFoundException exception =
                assertThrows(
                        EvidenceNotFoundException.class,
                        () -> evidenceService.getEvidence(
                                88L,
                                " OWNER@EXAMPLE.COM "
                        )
                );

        assertEquals(
                "Evidence file was not found",
                exception.getMessage()
        );

        verify(evidenceRepository)
                .findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                        88L,
                        OWNER_EMAIL
                );
    }

    private MockMultipartFile createTxtFile() {

        return new MockMultipartFile(
                "file",
                "invoice.txt",
                "text/plain",
                "Invoice evidence content"
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        );
    }

    private InvestigationCase createInvestigationCase() {

        User owner = new User();

        owner.setId(4L);
        owner.setFullName("Test Investigator");
        owner.setEmail(OWNER_EMAIL);
        owner.setPasswordHash("not-used-in-unit-test");
        owner.setActive(true);
        owner.beforeInsert();

        InvestigationCase investigationCase =
                new InvestigationCase();

        investigationCase.setId(9L);

        investigationCase.setCaseNumber(
                "TL-TEST-0001"
        );

        investigationCase.setTitle(
                "Invoice Investigation"
        );

        investigationCase.setDescription(
                "Investigate possible invoice manipulation."
        );

        investigationCase.setStatus(
                CaseStatus.OPEN
        );

        investigationCase.setPriority(
                CasePriority.HIGH
        );

        investigationCase.setOwner(owner);
        investigationCase.beforeInsert();

        return investigationCase;
    }

    private Evidence createEvidence(
            String sha256Hash
    ) {

        Evidence evidence = new Evidence();

        evidence.setId(31L);

        evidence.setOriginalFileName(
                "evidence.txt"
        );

        evidence.setStoredFileName(
                "evidence.txt"
        );

        evidence.setStorageRelativePath(
                "case-9/evidence.txt"
        );

        evidence.setFileType(
                EvidenceFileType.TXT
        );

        evidence.setContentType(
                "text/plain"
        );

        evidence.setFileSizeBytes(100L);

        evidence.setDescription(
                "Integrity test evidence"
        );

        evidence.setStatus(
                EvidenceStatus.UPLOADED
        );

        evidence.setSha256Hash(sha256Hash);

        evidence.setIntegrityStatus(
                EvidenceIntegrityStatus.NOT_VERIFIED
        );

        evidence.setInvestigationCase(
                createInvestigationCase()
        );

        evidence.beforeInsert();

        return evidence;
    }
}