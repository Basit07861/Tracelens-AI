package com.tracelens.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.ai.dto.AiEvidenceAnalysisHistoryResponse;
import com.tracelens.ai.dto.AiEvidenceAnalysisResponse;
import com.tracelens.ai.dto.AiEvidencePreviewResponse;
import com.tracelens.ai.dto.AiStatusResponse;
import com.tracelens.ai.service.AiEvidenceAnalysisService;
import com.tracelens.ai.service.AiEvidenceAnalysisStateService;
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

    private final AiEvidenceAnalysisStateService
            aiEvidenceAnalysisStateService;

    public AiController(
            AiStatusService aiStatusService,

            AiEvidencePreviewService
                    aiEvidencePreviewService,

            AiEvidenceAnalysisService
                    aiEvidenceAnalysisService,

            AiEvidenceAnalysisStateService
                    aiEvidenceAnalysisStateService
    ) {
        this.aiStatusService = aiStatusService;

        this.aiEvidencePreviewService =
                aiEvidencePreviewService;

        this.aiEvidenceAnalysisService =
                aiEvidenceAnalysisService;

        this.aiEvidenceAnalysisStateService =
                aiEvidenceAnalysisStateService;
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

    @PostMapping(
            "/evidence/{evidenceId}/analyses/regenerate"
    )
    public ResponseEntity<
            ApiResponse<AiEvidenceAnalysisResponse>>
            regenerateEvidenceAnalysis(

                    @PathVariable Long evidenceId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        AiEvidenceAnalysisResponse analysis =
                aiEvidenceAnalysisService
                        .regenerateAnalysis(
                                evidenceId,
                                jwt.getSubject()
                        );

        ApiResponse<AiEvidenceAnalysisResponse> response =
                ApiResponse.success(
                        "AI evidence analysis regenerated "
                        + "successfully",
                        analysis
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/analyses/{analysisId}"
    )
    public ResponseEntity<
            ApiResponse<AiEvidenceAnalysisResponse>>
            getEvidenceAnalysis(

                    @PathVariable Long analysisId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        AiEvidenceAnalysisResponse analysis =
                aiEvidenceAnalysisStateService
                        .getAnalysis(
                                analysisId,
                                jwt.getSubject()
                        );

        ApiResponse<AiEvidenceAnalysisResponse> response =
                ApiResponse.success(
                        "AI evidence analysis retrieved "
                        + "successfully",
                        analysis
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/evidence/{evidenceId}/analyses/latest"
    )
    public ResponseEntity<
            ApiResponse<AiEvidenceAnalysisResponse>>
            getLatestEvidenceAnalysis(

                    @PathVariable Long evidenceId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        AiEvidenceAnalysisResponse analysis =
                aiEvidenceAnalysisStateService
                        .getLatestAnalysis(
                                evidenceId,
                                jwt.getSubject()
                        );

        ApiResponse<AiEvidenceAnalysisResponse> response =
                ApiResponse.success(
                        "Latest AI evidence analysis "
                        + "retrieved successfully",
                        analysis
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/evidence/{evidenceId}/analyses"
    )
    public ResponseEntity<
            ApiResponse<
                    AiEvidenceAnalysisHistoryResponse>>
            getEvidenceAnalysisHistory(

                    @PathVariable Long evidenceId,

                    @RequestParam(
                            defaultValue = "0"
                    )
                    int page,

                    @RequestParam(
                            defaultValue = "10"
                    )
                    int size,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        AiEvidenceAnalysisHistoryResponse history =
                aiEvidenceAnalysisStateService
                        .getAnalysisHistory(
                                evidenceId,
                                jwt.getSubject(),
                                page,
                                size
                        );

        ApiResponse<
                AiEvidenceAnalysisHistoryResponse>
                response = ApiResponse.success(
                        "AI evidence analysis history "
                        + "retrieved successfully",
                        history
                );

        return ResponseEntity.ok(response);
    }
}