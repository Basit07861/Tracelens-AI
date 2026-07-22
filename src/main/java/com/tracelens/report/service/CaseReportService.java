package com.tracelens.report.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.ai.dto.AiEvidenceAnalysisResponse;
import com.tracelens.ai.entity.AiAnalysisStatus;
import com.tracelens.ai.repository.AiEvidenceAnalysisRepository;
import com.tracelens.ai.service.AiEvidenceAnalysisStateService;
import com.tracelens.evidence.dto.EvidenceResponse;
import com.tracelens.evidence.entity.Evidence;
import com.tracelens.evidence.repository.EvidenceRepository;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.intelligence.dto.EvidenceIntelligenceRunResponse;
import com.tracelens.intelligence.dto.ExtractedEntityResponse;
import com.tracelens.intelligence.dto.TimelineEventResponse;
import com.tracelens.intelligence.entity.IntelligenceRunStatus;
import com.tracelens.intelligence.repository.EvidenceIntelligenceRunRepository;
import com.tracelens.intelligence.service.EvidenceIntelligenceStateService;
import com.tracelens.investigation.dto.CaseResponse;
import com.tracelens.investigation.service.InvestigationCaseService;
import com.tracelens.note.dto.NoteResponse;
import com.tracelens.note.service.InvestigatorNoteService;
import com.tracelens.report.dto.CaseReportResponse;

@Service
public class CaseReportService {

    private static final String DISCLAIMER =
            "AI-generated findings are investigative aids "
            + "and must be independently verified.";

    private final InvestigationCaseService caseService;

    private final EvidenceRepository evidenceRepository;

    private final AiEvidenceAnalysisRepository
            analysisRepository;

    private final AiEvidenceAnalysisStateService
            analysisStateService;

    private final EvidenceIntelligenceRunRepository
            intelligenceRunRepository;

    private final EvidenceIntelligenceStateService
            intelligenceStateService;

    private final InvestigatorNoteService noteService;

    public CaseReportService(
            InvestigationCaseService caseService,

            EvidenceRepository evidenceRepository,

            AiEvidenceAnalysisRepository analysisRepository,

            AiEvidenceAnalysisStateService
                    analysisStateService,

            EvidenceIntelligenceRunRepository
                    intelligenceRunRepository,

            EvidenceIntelligenceStateService
                    intelligenceStateService,

            InvestigatorNoteService noteService
    ) {
        this.caseService = caseService;
        this.evidenceRepository = evidenceRepository;
        this.analysisRepository = analysisRepository;
        this.analysisStateService = analysisStateService;

        this.intelligenceRunRepository =
                intelligenceRunRepository;

        this.intelligenceStateService =
                intelligenceStateService;

        this.noteService = noteService;
    }

    @Transactional(readOnly = true)
    public CaseReportResponse generateReport(
            Long caseId,
            String authenticatedEmail
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        /*
         * This lookup must happen first. It verifies that the
         * authenticated investigator owns the requested case.
         */
        CaseResponse caseResponse =
                caseService.getCase(
                        caseId,
                        normalizedEmail
                );

        List<Evidence> evidenceEntities =
                evidenceRepository
                        .findAllByInvestigationCaseIdAndInvestigationCaseOwnerEmailIgnoreCaseOrderByUploadedAtAsc(
                                caseId,
                                normalizedEmail
                        );

        List<EvidenceResponse> evidenceResponses =
                evidenceEntities.stream()
                        .map(this::mapEvidence)
                        .toList();

        List<AiEvidenceAnalysisResponse> analyses =
                new ArrayList<>();

        List<ExtractedEntityResponse> entities =
                new ArrayList<>();

        List<TimelineEventResponse> timeline =
                new ArrayList<>();

        for (Evidence evidence : evidenceEntities) {

            addLatestCompletedAnalysis(
                    evidence,
                    normalizedEmail,
                    analyses
            );

            addLatestCompletedIntelligence(
                    evidence,
                    normalizedEmail,
                    entities,
                    timeline
            );
        }

        analyses.sort(
                Comparator
                        .comparingInt(this::riskRank)
                        .thenComparing(
                                AiEvidenceAnalysisResponse
                                        ::requestedAt,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
        );

        entities.sort(
                Comparator
                        .comparing(
                                (
                                        ExtractedEntityResponse
                                                entity
                                ) ->
                                        entity.entityType() == null
                                                ? ""
                                                : entity
                                                        .entityType()
                                                        .name()
                        )
                        .thenComparing(
                                ExtractedEntityResponse
                                        ::normalizedValue,
                                Comparator.nullsLast(
                                        String
                                                .CASE_INSENSITIVE_ORDER
                                )
                        )
        );

        timeline.sort(
                Comparator
                        .comparing(
                                TimelineEventResponse
                                        ::normalizedDateTime,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparingInt(
                                TimelineEventResponse
                                        ::sequenceNumber
                        )
        );

        List<NoteResponse> notes =
                noteService.getNotes(
                        caseId,
                        normalizedEmail
                );

        return new CaseReportResponse(
                caseResponse,
                List.copyOf(evidenceResponses),
                List.copyOf(analyses),
                List.copyOf(entities),
                List.copyOf(timeline),
                List.copyOf(notes),
                Instant.now(),
                DISCLAIMER
        );
    }

    private void addLatestCompletedAnalysis(
            Evidence evidence,
            String normalizedEmail,
            List<AiEvidenceAnalysisResponse> analyses
    ) {

        analysisRepository
                .findFirstByEvidenceIdAndStatusOrderByRequestedAtDesc(
                        evidence.getId(),
                        AiAnalysisStatus.COMPLETED
                )
                .ifPresent(analysis -> {

                    AiEvidenceAnalysisResponse response =
                            analysisStateService.getAnalysis(
                                    analysis.getId(),
                                    normalizedEmail
                            );

                    analyses.add(response);
                });
    }

    private void addLatestCompletedIntelligence(
            Evidence evidence,
            String normalizedEmail,
            List<ExtractedEntityResponse> entities,
            List<TimelineEventResponse> timeline
    ) {

        intelligenceRunRepository
                .findFirstByEvidenceIdAndStatusOrderByRequestedAtDesc(
                        evidence.getId(),
                        IntelligenceRunStatus.COMPLETED
                )
                .ifPresent(run -> {

                    EvidenceIntelligenceRunResponse response =
                            intelligenceStateService.getRun(
                                    run.getId(),
                                    normalizedEmail
                            );

                    if (response.entities() != null) {
                        entities.addAll(response.entities());
                    }

                    if (response.timelineEvents() != null) {
                        timeline.addAll(
                                response.timelineEvents()
                        );
                    }
                });
    }

    private EvidenceResponse mapEvidence(
            Evidence evidence
    ) {

        var investigationCase =
                evidence.getInvestigationCase();

        return new EvidenceResponse(
                evidence.getId(),
                investigationCase.getId(),
                investigationCase.getCaseNumber(),
                evidence.getOriginalFileName(),
                evidence.getFileType(),
                evidence.getContentType(),
                evidence.getFileSizeBytes(),
                evidence.getDescription(),
                evidence.getStatus(),
                evidence.getSha256Hash(),
                evidence.getIntegrityStatus(),
                evidence.getLastIntegrityVerifiedAt(),
                evidence.getUploadedAt(),
                evidence.getUpdatedAt()
        );
    }

    private int riskRank(
            AiEvidenceAnalysisResponse analysis
    ) {

        if (analysis.riskLevel() == null) {
            return 5;
        }

        return switch (analysis.riskLevel().name()) {
            case "CRITICAL" -> 1;
            case "HIGH" -> 2;
            case "MEDIUM" -> 3;
            case "LOW" -> 4;
            default -> 5;
        };
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidRequestException(
                    "Authenticated user is unavailable"
            );
        }

        return email
                .strip()
                .toLowerCase(Locale.ROOT);
    }
}