package com.tracelens.report.dto;

import java.time.Instant;
import java.util.List;

import com.tracelens.ai.dto.AiEvidenceAnalysisResponse;
import com.tracelens.evidence.dto.EvidenceResponse;
import com.tracelens.intelligence.dto.ExtractedEntityResponse;
import com.tracelens.intelligence.dto.TimelineEventResponse;
import com.tracelens.investigation.dto.CaseResponse;
import com.tracelens.note.dto.NoteResponse;

public record CaseReportResponse(

        CaseResponse investigationCase,

        List<EvidenceResponse> evidence,

        List<AiEvidenceAnalysisResponse> analyses,

        List<ExtractedEntityResponse> entities,

        List<TimelineEventResponse> timeline,

        List<NoteResponse> notes,

        Instant generatedAt,

        String disclaimer
) {
}