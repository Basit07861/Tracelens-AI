package com.tracelens.evidence.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tracelens.common.ApiResponse;
import com.tracelens.evidence.dto.EvidenceResponse;
import com.tracelens.evidence.service.EvidenceService;

@RestController
@RequestMapping("/api")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(
            EvidenceService evidenceService
    ) {
        this.evidenceService = evidenceService;
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
}