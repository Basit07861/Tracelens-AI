package com.tracelens.evidence.extraction;

import org.springframework.core.io.Resource;

import com.tracelens.evidence.entity.EvidenceFileType;

public interface EvidenceTextExtractor {

    EvidenceFileType supportedFileType();

    String extract(Resource resource);
}