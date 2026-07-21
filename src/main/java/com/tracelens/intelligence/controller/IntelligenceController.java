package com.tracelens.intelligence.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.common.ApiResponse;
import com.tracelens.intelligence.dto.EvidenceIntelligenceRunResponse;
import com.tracelens.intelligence.service.EvidenceIntelligenceService;

@RestController
@RequestMapping("/api/intelligence")
public class IntelligenceController {

    private final EvidenceIntelligenceService
            intelligenceService;

    public IntelligenceController(
            EvidenceIntelligenceService intelligenceService
    ) {
        this.intelligenceService =
                intelligenceService;
    }

    @PostMapping("/evidence/{evidenceId}/runs")
    public ResponseEntity<
            ApiResponse<EvidenceIntelligenceRunResponse>>
            generateIntelligence(

                    @PathVariable Long evidenceId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        EvidenceIntelligenceRunResponse result =
                intelligenceService
                        .generateIntelligence(
                                evidenceId,
                                jwt.getSubject()
                        );

        ApiResponse<EvidenceIntelligenceRunResponse>
                response =
                        ApiResponse.success(
                                "Evidence entities and timeline "
                                + "generated successfully",
                                result
                        );

        return ResponseEntity.ok(response);
    }
}