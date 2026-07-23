package com.tracelens.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.common.ApiResponse;
import com.tracelens.dashboard.dto.DashboardResponse;
import com.tracelens.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>>
            getDashboard(
                    @AuthenticationPrincipal Jwt jwt
            ) {

        DashboardResponse dashboard =
                dashboardService.getDashboard(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dashboard analytics retrieved successfully",
                        dashboard
                )
        );
    }
}