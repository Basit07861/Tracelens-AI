package com.tracelens.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.ai.dto.AiEvidenceAnalysisResponse;
import com.tracelens.ai.dto.AiEvidencePreviewResponse;
import com.tracelens.ai.dto.AiStatusResponse;
import com.tracelens.ai.service.AiEvidenceAnalysisService;
import com.tracelens.ai.service.AiEvidencePreviewService;
import com.tracelens.ai.service.AiStatusService;
import com.tracelens.common.ApiResponse;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiStatusService aiStatusService;

    private final AiEvidencePreviewService
            aiEvidencePreviewService;

    private final AiEvidenceAnalysisService
            aiEvidenceAnalysisService;

    public AiController(
            AiStatusService aiStatusService,

            AiEvidencePreviewService
                    aiEvidencePreviewService,

            AiEvidenceAnalysisService
                    aiEvidenceAnalysisService
    ) {
        this.aiStatusService = aiStatusService;

        this.aiEvidencePreviewService =
                aiEvidencePreviewService;

        this.aiEvidenceAnalysisService =
                aiEvidenceAnalysisService;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<AiStatusResponse>>
            getAiStatus() {

        AiStatusResponse status =
                aiStatusService.checkStatus();

        ApiResponse<AiStatusResponse> response =
                ApiResponse.success(
                        "AI connectivity check completed",
                        status
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            "/evidence/{evidenceId}/preview"
    )
    public ResponseEntity<
            ApiResponse<AiEvidencePreviewResponse>>
            generateEvidencePreview(

                    @PathVariable Long evidenceId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        AiEvidencePreviewResponse preview =
                aiEvidencePreviewService
                        .generatePreview(
                                evidenceId,
                                jwt.getSubject()
                        );

        ApiResponse<AiEvidencePreviewResponse> response =
                ApiResponse.success(
                        "Structured AI evidence preview "
                        + "generated successfully",
                        preview
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            "/evidence/{evidenceId}/analyses"
    )
    public ResponseEntity<
            ApiResponse<AiEvidenceAnalysisResponse>>
            generateEvidenceAnalysis(

                    @PathVariable Long evidenceId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        AiEvidenceAnalysisResponse analysis =
                aiEvidenceAnalysisService
                        .generateAnalysis(
                                evidenceId,
                                jwt.getSubject()
                        );

        ApiResponse<AiEvidenceAnalysisResponse> response =
                ApiResponse.success(
                        "Persistent AI evidence analysis "
                        + "generated successfully",
                        analysis
                );

        return ResponseEntity.ok(response);
    }
}