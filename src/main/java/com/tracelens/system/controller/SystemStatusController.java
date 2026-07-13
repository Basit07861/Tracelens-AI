package com.tracelens.system.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.common.ApiResponse;
import com.tracelens.system.service.SystemStatusService;

@RestController
@RequestMapping("/api/system")
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    public SystemStatusController(
            SystemStatusService systemStatusService
    ) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStatus() {

        Map<String, Object> systemStatus =
                systemStatusService.getSystemStatus();

        ApiResponse<Map<String, Object>> response =
                ApiResponse.success(
                        "TraceLens backend is running successfully",
                        systemStatus
                );

        return ResponseEntity.ok(response);
    }
}