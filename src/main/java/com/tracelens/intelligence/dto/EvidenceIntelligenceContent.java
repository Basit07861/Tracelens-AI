package com.tracelens.intelligence.dto;

import java.util.List;

public record EvidenceIntelligenceContent(
        List<IntelligenceEntityContent> entities,
        List<IntelligenceTimelineEventContent>
                timelineEvents
) {
}