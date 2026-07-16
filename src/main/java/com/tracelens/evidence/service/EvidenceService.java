package com.tracelens.evidence.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tracelens.evidence.dto.EvidenceResponse;
import com.tracelens.evidence.entity.Evidence;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.evidence.entity.EvidenceStatus;
import com.tracelens.evidence.repository.EvidenceRepository;
import com.tracelens.evidence.storage.EvidenceStorageService;
import com.tracelens.evidence.storage.StoredEvidenceFile;
import com.tracelens.exception.CaseNotFoundException;
import com.tracelens.investigation.entity.InvestigationCase;
import com.tracelens.investigation.repository.InvestigationCaseRepository;

@Service
public class EvidenceService {

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
            evidence.setFileSizeBytes(file.getSize());
            evidence.setDescription(normalizedDescription);
            evidence.setStatus(EvidenceStatus.UPLOADED);

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

    private InvestigationCase findOwnedCase(
            Long caseId,
            String authenticatedEmail
    ) {

        String normalizedEmail =
                authenticatedEmail
                        .trim()
                        .toLowerCase(Locale.ROOT);

        return caseRepository
                .findByIdAndOwnerEmailIgnoreCase(
                        caseId,
                        normalizedEmail
                )
                .orElseThrow(() -> new CaseNotFoundException(
                        "Investigation case was not found"
                ));
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
                evidence.getUploadedAt(),
                evidence.getUpdatedAt()
        );
    }
}