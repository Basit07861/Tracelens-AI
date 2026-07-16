package com.tracelens.evidence.storage;

import org.springframework.core.io.Resource;

public record EvidenceFileResource(
        Resource resource,
        String originalFileName,
        String contentType,
        long fileSizeBytes
) {
}