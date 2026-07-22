package com.tracelens.report.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.common.ApiResponse;
import com.tracelens.report.dto.CaseReportResponse;
import com.tracelens.report.service.CaseReportService;

@RestController
@RequestMapping("/api/cases")
public class CaseReportController {

    private final CaseReportService reportService;

    public CaseReportController(
            CaseReportService reportService
    ) {
        this.reportService = reportService;
    }

    @GetMapping("/{caseId}/report")
    public ApiResponse<CaseReportResponse>
            getCaseReport(

                    @PathVariable Long caseId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        CaseReportResponse response =
                reportService.generateReport(
                        caseId,
                        jwt.getSubject()
                );

        return ApiResponse.success(
                "Investigation case report generated successfully",
                response
        );
    }
}