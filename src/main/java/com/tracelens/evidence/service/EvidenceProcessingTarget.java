package com.tracelens.evidence.service;

import com.tracelens.evidence.entity.EvidenceFileType;

public record EvidenceProcessingTarget(
        Long evidenceId,
        EvidenceFileType fileType,
        String storageRelativePath
) {
}