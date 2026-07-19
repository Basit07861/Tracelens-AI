package com.tracelens.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.ai.dto.AiStatusResponse;
import com.tracelens.ai.service.AiStatusService;
import com.tracelens.common.ApiResponse;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiStatusService aiStatusService;

    public AiController(
            AiStatusService aiStatusService
    ) {
        this.aiStatusService = aiStatusService;
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
}