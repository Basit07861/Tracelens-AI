package com.tracelens.evidence.service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.tracelens.common.PageResponse;
import com.tracelens.evidence.dto.EvidenceResponse;
import com.tracelens.evidence.entity.Evidence;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.evidence.entity.EvidenceIntegrityStatus;
import com.tracelens.evidence.entity.EvidenceStatus;
import com.tracelens.evidence.repository.EvidenceRepository;
import com.tracelens.evidence.storage.EvidenceFileResource;
import com.tracelens.evidence.storage.EvidenceStorageService;
import com.tracelens.evidence.storage.StoredEvidenceFile;
import com.tracelens.exception.CaseNotFoundException;
import com.tracelens.exception.DuplicateEvidenceException;
import com.tracelens.exception.EvidenceNotFoundException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.investigation.entity.InvestigationCase;
import com.tracelens.investigation.repository.InvestigationCaseRepository;

@Service
public class EvidenceService {

    private static final int MAXIMUM_PAGE_SIZE = 100;

    private final EvidenceRepository evidenceRepository;
    private final InvestigationCaseRepository caseRepository;
    private final EvidenceFileValidator fileValidator;
    private final EvidenceStorageService storageService;

    public EvidenceService(
            EvidenceRepository evidenceRepository,
            InvestigationCaseRepository caseRepository,
            EvidenceFileValidator fileValidator,
            EvidenceStorageService storageService
    ) {
        this.evidenceRepository = evidenceRepository;
        this.caseRepository = caseRepository;
        this.fileValidator = fileValidator;
        this.storageService = storageService;
    }

    @Transactional
    public EvidenceResponse uploadEvidence(
            Long caseId,
            MultipartFile file,
            String description,
            String authenticatedEmail
    ) {

        InvestigationCase investigationCase =
                findOwnedCase(
                        caseId,
                        authenticatedEmail
                );

        EvidenceFileType fileType =
                fileValidator
                        .validateAndResolveFileType(file);

        String originalFileName =
                fileValidator.sanitizeOriginalFileName(
                        file.getOriginalFilename()
                );

        String contentType =
                fileValidator.normalizeContentType(
                        file.getContentType()
                );

        String normalizedDescription =
                fileValidator.normalizeDescription(
                        description
                );

        StoredEvidenceFile storedFile =
                storageService.store(
                        investigationCase.getId(),
                        file,
                        fileType
                );

        if (
                evidenceRepository
                        .existsByInvestigationCaseIdAndSha256Hash(
                                investigationCase.getId(),
                                storedFile.sha256Hash()
                        )
        ) {

            storageService.deleteQuietly(
                    storedFile.relativePath()
            );

            throw new DuplicateEvidenceException(
                    "This evidence file already exists "
                    + "in the selected investigation case"
            );
        }

        try {
            Evidence evidence = new Evidence();

            evidence.setOriginalFileName(
                    originalFileName
            );

            evidence.setStoredFileName(
                    storedFile.storedFileName()
            );

            evidence.setStorageRelativePath(
                    storedFile.relativePath()
            );

            evidence.setFileType(fileType);
            evidence.setContentType(contentType);

            evidence.setFileSizeBytes(
                    storedFile.fileSizeBytes()
            );

            evidence.setDescription(
                    normalizedDescription
            );

            evidence.setStatus(
                    EvidenceStatus.UPLOADED
            );

            evidence.setSha256Hash(
                    storedFile.sha256Hash()
            );

            evidence.setIntegrityStatus(
                    EvidenceIntegrityStatus.VERIFIED
            );

            evidence.setLastIntegrityVerifiedAt(
                    Instant.now()
            );

            evidence.setInvestigationCase(
                    investigationCase
            );

            Evidence savedEvidence =
                    evidenceRepository.saveAndFlush(
                            evidence
                    );

            return mapToResponse(savedEvidence);
        }
        catch (RuntimeException exception) {

            storageService.deleteQuietly(
                    storedFile.relativePath()
            );

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<EvidenceResponse>
            getEvidenceForCase(

                    Long caseId,
                    String authenticatedEmail,
                    int page,
                    int size
            ) {

        validatePagination(page, size);

        findOwnedCase(
                caseId,
                authenticatedEmail
        );

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "uploadedAt"
                )
        );

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        Page<EvidenceResponse> responsePage =
                evidenceRepository
                        .findAllByInvestigationCaseIdAndInvestigationCaseOwnerEmailIgnoreCase(
                                caseId,
                                normalizedEmail,
                                pageRequest
                        )
                        .map(this::mapToResponse);

        return PageResponse.from(
                responsePage,
                "uploadedAt",
                "desc"
        );
    }

    @Transactional(readOnly = true)
    public EvidenceResponse getEvidence(
            Long evidenceId,
            String authenticatedEmail
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        return mapToResponse(evidence);
    }

    @Transactional(readOnly = true)
    public EvidenceFileResource downloadEvidence(
            Long evidenceId,
            String authenticatedEmail
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        Resource resource =
                storageService.loadAsResource(
                        evidence.getStorageRelativePath()
                );

        return new EvidenceFileResource(
                resource,
                evidence.getOriginalFileName(),
                evidence.getContentType(),
                evidence.getFileSizeBytes()
        );
    }

    @Transactional
    public void deleteEvidence(
            Long evidenceId,
            String authenticatedEmail
    ) {

        Evidence evidence = findOwnedEvidence(
                evidenceId,
                authenticatedEmail
        );

        String relativePath =
                evidence.getStorageRelativePath();

        evidenceRepository.delete(evidence);
        evidenceRepository.flush();

        deleteStoredFileAfterCommit(relativePath);
    }

    private InvestigationCase findOwnedCase(
            Long caseId,
            String authenticatedEmail
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        return caseRepository
                .findByIdAndOwnerEmailIgnoreCase(
                        caseId,
                        normalizedEmail
                )
                .orElseThrow(() -> new CaseNotFoundException(
                        "Investigation case was not found"
                ));
    }

    private Evidence findOwnedEvidence(
            Long evidenceId,
            String authenticatedEmail
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        return evidenceRepository
                .findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                        evidenceId,
                        normalizedEmail
                )
                .orElseThrow(
                        () -> new EvidenceNotFoundException(
                                "Evidence file was not found"
                        )
                );
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new InvalidRequestException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > MAXIMUM_PAGE_SIZE) {
            throw new InvalidRequestException(
                    "Page size must be between 1 and "
                    + MAXIMUM_PAGE_SIZE
            );
        }
    }

    private void deleteStoredFileAfterCommit(
            String relativePath
    ) {

        if (
                TransactionSynchronizationManager
                        .isSynchronizationActive()
        ) {

            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {

                                @Override
                                public void afterCommit() {

                                    storageService.deleteQuietly(
                                            relativePath
                                    );
                                }
                            }
                    );

            return;
        }

        storageService.deleteQuietly(relativePath);
    }

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private EvidenceResponse mapToResponse(
            Evidence evidence
    ) {

        InvestigationCase investigationCase =
                evidence.getInvestigationCase();

        return new EvidenceResponse(
                evidence.getId(),
                investigationCase.getId(),
                investigationCase.getCaseNumber(),
                evidence.getOriginalFileName(),
                evidence.getFileType(),
                evidence.getContentType(),
                evidence.getFileSizeBytes(),
                evidence.getDescription(),
                evidence.getStatus(),
                evidence.getSha256Hash(),
                evidence.getIntegrityStatus(),
                evidence.getLastIntegrityVerifiedAt(),
                evidence.getUploadedAt(),
                evidence.getUpdatedAt()
        );
    }
}