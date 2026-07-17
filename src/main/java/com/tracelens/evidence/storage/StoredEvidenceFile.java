package com.tracelens.evidence.storage;

public record StoredEvidenceFile(
        String storedFileName,
        String relativePath,
        long fileSizeBytes,
        String sha256Hash
) {
}