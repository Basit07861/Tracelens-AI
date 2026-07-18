package com.tracelens.evidence.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tracelens.common.ApiResponse;
import com.tracelens.common.PageResponse;
import com.tracelens.evidence.dto.EvidenceExtractionResponse;
import com.tracelens.evidence.dto.EvidenceIntegrityResponse;
import com.tracelens.evidence.dto.EvidenceResponse;
import com.tracelens.evidence.service.EvidenceProcessingService;
import com.tracelens.evidence.service.EvidenceService;
import com.tracelens.evidence.storage.EvidenceFileResource;

@RestController
@RequestMapping("/api")
public class EvidenceController {

    private final EvidenceService evidenceService;

    private final EvidenceProcessingService
            evidenceProcessingService;

    public EvidenceController(
            EvidenceService evidenceService,
            EvidenceProcessingService
                    evidenceProcessingService
    ) {
        this.evidenceService = evidenceService;
        this.evidenceProcessingService =
                evidenceProcessingService;
    }

    @PostMapping(
            path = "/cases/{caseId}/evidence",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<EvidenceResponse>>
            uploadEvidence(

                    @PathVariable Long caseId,

                    @RequestParam(
                            name = "file",
                            required = false
                    )
                    MultipartFile file,

                    @RequestParam(
                            name = "description",
                            required = false
                    )
                    String description,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        EvidenceResponse uploadedEvidence =
                evidenceService.uploadEvidence(
                        caseId,
                        file,
                        description,
                        jwt.getSubject()
                );

        ApiResponse<EvidenceResponse> response =
                ApiResponse.success(
                        "Evidence uploaded successfully",
                        uploadedEvidence
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/cases/{caseId}/evidence")
    public ResponseEntity<
            ApiResponse<PageResponse<EvidenceResponse>>>
            getEvidenceForCase(

                    @PathVariable Long caseId,

                    @RequestParam(defaultValue = "0")
                    int page,

                    @RequestParam(defaultValue = "10")
                    int size,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        PageResponse<EvidenceResponse> evidence =
                evidenceService.getEvidenceForCase(
                        caseId,
                        jwt.getSubject(),
                        page,
                        size
                );

        ApiResponse<PageResponse<EvidenceResponse>>
                response = ApiResponse.success(
                        "Evidence files retrieved successfully",
                        evidence
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/evidence/{evidenceId}")
    public ResponseEntity<ApiResponse<EvidenceResponse>>
            getEvidence(

                    @PathVariable Long evidenceId,
                    @AuthenticationPrincipal Jwt jwt
            ) {

        EvidenceResponse evidence =
                evidenceService.getEvidence(
                        evidenceId,
                        jwt.getSubject()
                );

        ApiResponse<EvidenceResponse> response =
                ApiResponse.success(
                        "Evidence metadata retrieved successfully",
                        evidence
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/evidence/{evidenceId}/download")
    public ResponseEntity<Resource> downloadEvidence(

            @PathVariable Long evidenceId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        EvidenceFileResource evidenceFile =
                evidenceService.downloadEvidence(
                        evidenceId,
                        jwt.getSubject()
                );

        ContentDisposition contentDisposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                evidenceFile.originalFileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        MediaType mediaType = MediaType.parseMediaType(
                evidenceFile.contentType()
        );

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .contentLength(
                        evidenceFile.fileSizeBytes()
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(evidenceFile.resource());
    }

    @PostMapping(
            "/evidence/{evidenceId}/verify-integrity"
    )
    public ResponseEntity<
            ApiResponse<EvidenceIntegrityResponse>>
            verifyEvidenceIntegrity(

                    @PathVariable Long evidenceId,
                    @AuthenticationPrincipal Jwt jwt
            ) {

        EvidenceIntegrityResponse verification =
                evidenceService.verifyEvidenceIntegrity(
                        evidenceId,
                        jwt.getSubject()
                );

        String message = verification.matches()
                ? "Evidence integrity verified successfully"
                : "Evidence integrity mismatch detected";

        ApiResponse<EvidenceIntegrityResponse> response =
                ApiResponse.success(
                        message,
                        verification
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            "/evidence/{evidenceId}/extract-text"
    )
    public ResponseEntity<
            ApiResponse<EvidenceExtractionResponse>>
            extractEvidenceText(

                    @PathVariable Long evidenceId,
                    @AuthenticationPrincipal Jwt jwt
            ) {

        EvidenceExtractionResponse extraction =
                evidenceProcessingService.extractText(
                        evidenceId,
                        jwt.getSubject()
                );

        ApiResponse<EvidenceExtractionResponse> response =
                ApiResponse.success(
                        "Evidence text extracted successfully",
                        extraction
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/evidence/{evidenceId}/extracted-text"
    )
    public ResponseEntity<
            ApiResponse<EvidenceExtractionResponse>>
            getExtractedText(

                    @PathVariable Long evidenceId,
                    @AuthenticationPrincipal Jwt jwt
            ) {

        EvidenceExtractionResponse extraction =
                evidenceProcessingService
                        .getExtractionResult(
                                evidenceId,
                                jwt.getSubject()
                        );

        ApiResponse<EvidenceExtractionResponse> response =
                ApiResponse.success(
                        "Evidence extraction information "
                        + "retrieved successfully",
                        extraction
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/evidence/{evidenceId}")
    public ResponseEntity<ApiResponse<Void>>
            deleteEvidence(

                    @PathVariable Long evidenceId,
                    @AuthenticationPrincipal Jwt jwt
            ) {

        evidenceService.deleteEvidence(
                evidenceId,
                jwt.getSubject()
        );

        ApiResponse<Void> response =
                ApiResponse.<Void>success(
                        "Evidence deleted successfully",
                        null
                );

        return ResponseEntity.ok(response);
    }
}