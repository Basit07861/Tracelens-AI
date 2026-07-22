package com.tracelens.intelligence.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.common.ApiResponse;
import com.tracelens.intelligence.dto.EvidenceIntelligenceRunHistoryResponse;
import com.tracelens.intelligence.dto.EvidenceIntelligenceRunResponse;
import com.tracelens.intelligence.dto.ExtractedEntityPageResponse;
import com.tracelens.intelligence.dto.TimelineEventPageResponse;
import com.tracelens.intelligence.entity.ExtractedEntityType;
import com.tracelens.intelligence.entity.TimelineEventCertainty;
import com.tracelens.intelligence.entity.TimelineTemporalPrecision;
import com.tracelens.intelligence.service.EvidenceIntelligenceService;
import com.tracelens.intelligence.service.EvidenceIntelligenceStateService;

@RestController
@RequestMapping("/api/intelligence")
public class IntelligenceController {

    private final EvidenceIntelligenceService
            intelligenceService;

    private final EvidenceIntelligenceStateService
            stateService;

    public IntelligenceController(
            EvidenceIntelligenceService intelligenceService,
            EvidenceIntelligenceStateService stateService
    ) {
        this.intelligenceService = intelligenceService;
        this.stateService = stateService;
    }

    /*
     * Generates the first intelligence run.
     *
     * This endpoint rejects the request when the evidence
     * already has an intelligence-run history.
     */
    @PostMapping(
            "/evidence/{evidenceId}/runs"
    )
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

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Evidence entities and timeline "
                        + "generated successfully",
                        result
                )
        );
    }

    /*
     * Generates a new run while preserving every
     * previous intelligence run.
     */
    @PostMapping(
            "/evidence/{evidenceId}/runs/regenerate"
    )
    public ResponseEntity<
            ApiResponse<EvidenceIntelligenceRunResponse>>
            regenerateIntelligence(

                    @PathVariable Long evidenceId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        EvidenceIntelligenceRunResponse result =
                intelligenceService
                        .regenerateIntelligence(
                                evidenceId,
                                jwt.getSubject()
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Evidence intelligence regenerated "
                        + "successfully",
                        result
                )
        );
    }

    /*
     * Retrieves one complete owned intelligence run,
     * including its entities and timeline events.
     */
    @GetMapping(
            "/runs/{runId}"
    )
    public ResponseEntity<
            ApiResponse<EvidenceIntelligenceRunResponse>>
            getRun(

                    @PathVariable Long runId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        EvidenceIntelligenceRunResponse result =
                stateService.getRun(
                        runId,
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Intelligence run retrieved "
                        + "successfully",
                        result
                )
        );
    }

    /*
     * Retrieves the newest intelligence run for
     * one owned evidence file.
     */
    @GetMapping(
            "/evidence/{evidenceId}/runs/latest"
    )
    public ResponseEntity<
            ApiResponse<EvidenceIntelligenceRunResponse>>
            getLatestRun(

                    @PathVariable Long evidenceId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        EvidenceIntelligenceRunResponse result =
                stateService.getLatestRun(
                        evidenceId,
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Latest intelligence run retrieved "
                        + "successfully",
                        result
                )
        );
    }

    /*
     * Retrieves paginated intelligence-run history.
     *
     * Example:
     * GET /api/intelligence/evidence/7/runs?page=0&size=10
     */
    @GetMapping(
            "/evidence/{evidenceId}/runs"
    )
    public ResponseEntity<
            ApiResponse<EvidenceIntelligenceRunHistoryResponse>>
            getRunHistory(

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

        EvidenceIntelligenceRunHistoryResponse result =
                stateService.getRunHistory(
                        evidenceId,
                        jwt.getSubject(),
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Intelligence run history retrieved "
                        + "successfully",
                        result
                )
        );
    }

    /*
     * Retrieves paginated entities for a run.
     *
     * entityType is optional.
     *
     * Example:
     * GET /api/intelligence/runs/7/entities
     *
     * GET /api/intelligence/runs/7/entities
     *     ?entityType=ORGANIZATION
     */
    @GetMapping(
            "/runs/{runId}/entities"
    )
    public ResponseEntity<
            ApiResponse<ExtractedEntityPageResponse>>
            getEntities(

                    @PathVariable Long runId,

                    @RequestParam(
                            required = false
                    )
                    ExtractedEntityType entityType,

                    @RequestParam(
                            defaultValue = "0"
                    )
                    int page,

                    @RequestParam(
                            defaultValue = "20"
                    )
                    int size,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        ExtractedEntityPageResponse result =
                stateService.getEntities(
                        runId,
                        jwt.getSubject(),
                        entityType,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Extracted entities retrieved "
                        + "successfully",
                        result
                )
        );
    }

    /*
     * Retrieves paginated timeline events.
     *
     * certainty and temporalPrecision are optional.
     *
     * Example:
     * GET /api/intelligence/runs/7/timeline-events
     *
     * GET /api/intelligence/runs/7/timeline-events
     *     ?certainty=OBSERVED
     *
     * GET /api/intelligence/runs/7/timeline-events
     *     ?temporalPrecision=DATE_TIME
     */
    @GetMapping(
            "/runs/{runId}/timeline-events"
    )
    public ResponseEntity<
            ApiResponse<TimelineEventPageResponse>>
            getTimelineEvents(

                    @PathVariable Long runId,

                    @RequestParam(
                            required = false
                    )
                    TimelineEventCertainty certainty,

                    @RequestParam(
                            required = false
                    )
                    TimelineTemporalPrecision temporalPrecision,

                    @RequestParam(
                            defaultValue = "0"
                    )
                    int page,

                    @RequestParam(
                            defaultValue = "20"
                    )
                    int size,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        TimelineEventPageResponse result =
                stateService.getTimelineEvents(
                        runId,
                        jwt.getSubject(),
                        certainty,
                        temporalPrecision,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Timeline events retrieved "
                        + "successfully",
                        result
                )
        );
    }
}