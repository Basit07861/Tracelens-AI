package com.tracelens.investigation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.common.ApiResponse;
import com.tracelens.investigation.dto.CaseResponse;
import com.tracelens.investigation.dto.CreateCaseRequest;
import com.tracelens.investigation.dto.UpdateCaseRequest;
import com.tracelens.investigation.dto.UpdateCaseStatusRequest;
import com.tracelens.investigation.service.InvestigationCaseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cases")
public class InvestigationCaseController {

    private final InvestigationCaseService caseService;

    public InvestigationCaseController(
            InvestigationCaseService caseService
    ) {
        this.caseService = caseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CaseResponse>> createCase(
            @Valid @RequestBody CreateCaseRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        CaseResponse createdCase =
                caseService.createCase(
                        request,
                        jwt.getSubject()
                );

        ApiResponse<CaseResponse> response =
                ApiResponse.success(
                        "Investigation case created successfully",
                        createdCase
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<ApiResponse<CaseResponse>> getCase(
            @PathVariable Long caseId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        CaseResponse investigationCase =
                caseService.getCase(
                        caseId,
                        jwt.getSubject()
                );

        ApiResponse<CaseResponse> response =
                ApiResponse.success(
                        "Investigation case retrieved successfully",
                        investigationCase
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{caseId}")
    public ResponseEntity<ApiResponse<CaseResponse>> updateCase(
            @PathVariable Long caseId,
            @Valid @RequestBody UpdateCaseRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        CaseResponse updatedCase =
                caseService.updateCase(
                        caseId,
                        request,
                        jwt.getSubject()
                );

        ApiResponse<CaseResponse> response =
                ApiResponse.success(
                        "Investigation case updated successfully",
                        updatedCase
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{caseId}/status")
    public ResponseEntity<ApiResponse<CaseResponse>>
            updateCaseStatus(
                    @PathVariable Long caseId,
                    @Valid @RequestBody
                    UpdateCaseStatusRequest request,
                    @AuthenticationPrincipal Jwt jwt
            ) {

        CaseResponse updatedCase =
                caseService.updateCaseStatus(
                        caseId,
                        request,
                        jwt.getSubject()
                );

        ApiResponse<CaseResponse> response =
                ApiResponse.success(
                        "Investigation case status updated successfully",
                        updatedCase
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{caseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCase(
            @PathVariable Long caseId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        caseService.deleteCase(
                caseId,
                jwt.getSubject()
        );

        ApiResponse<Void> response =
                ApiResponse.<Void>success(
                        "Investigation case deleted successfully",
                        null
                );

        return ResponseEntity.ok(response);
    }
}