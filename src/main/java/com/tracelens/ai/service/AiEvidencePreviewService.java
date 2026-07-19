package com.tracelens.ai.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.tracelens.ai.dto.AiEvidencePreviewContent;
import com.tracelens.ai.dto.AiEvidencePreviewResponse;
import com.tracelens.ai.entity.AiPreviewRiskLevel;
import com.tracelens.evidence.dto.EvidenceExtractionResponse;
import com.tracelens.evidence.entity.EvidenceStatus;
import com.tracelens.evidence.service.EvidenceProcessingService;
import com.tracelens.exception.InvalidRequestException;

@Service
public class AiEvidencePreviewService {

    private final ChatClient chatClient;

    private final EvidenceProcessingService
            evidenceProcessingService;

    private final PromptTemplate previewPromptTemplate;

    public AiEvidencePreviewService(
            ChatClient traceLensChatClient,

            EvidenceProcessingService
                    evidenceProcessingService,

            @Value(
                    "classpath:/prompts/"
                    + "evidence-preview-user.st"
            )
            Resource previewPromptResource
    ) {
        this.chatClient = traceLensChatClient;

        this.evidenceProcessingService =
                evidenceProcessingService;

        this.previewPromptTemplate =
                new PromptTemplate(
                        previewPromptResource
                );
    }

    public AiEvidencePreviewResponse generatePreview(
            Long evidenceId,
            String authenticatedEmail
    ) {

        EvidenceExtractionResponse extraction =
                evidenceProcessingService
                        .getExtractionResult(
                                evidenceId,
                                authenticatedEmail
                        );

        validateExtraction(extraction);

        String renderedPrompt =
                previewPromptTemplate.render(
                        Map.of(
                                "fileName",
                                extraction.originalFileName(),

                                "fileType",
                                extraction.fileType().name(),

                                "caseNumber",
                                extraction.caseNumber(),

                                "evidenceText",
                                extraction.extractedText()
                        )
                );

        AiEvidencePreviewContent preview =
                chatClient
                        .prompt()
                        .user(renderedPrompt)
                        .call()
                        .entity(
                                AiEvidencePreviewContent.class
                        );

        return createResponse(
                extraction,
                preview
        );
    }

    private void validateExtraction(
            EvidenceExtractionResponse extraction
    ) {

        if (extraction == null) {
            throw new InvalidRequestException(
                    "Evidence extraction information "
                    + "is unavailable"
            );
        }

        if (
                extraction.status()
                != EvidenceStatus.PROCESSED
        ) {
            throw new InvalidRequestException(
                    "Evidence text must be successfully "
                    + "extracted before generating "
                    + "an AI preview"
            );
        }

        if (
                extraction.extractedText() == null
                || extraction.extractedText().isBlank()
        ) {
            throw new InvalidRequestException(
                    "The evidence does not contain "
                    + "extracted text for AI analysis"
            );
        }
    }

    private AiEvidencePreviewResponse createResponse(
            EvidenceExtractionResponse extraction,
            AiEvidencePreviewContent preview
    ) {

        if (preview == null) {
            throw new IllegalStateException(
                    "AI service returned no structured preview"
            );
        }

        String summary =
                preview.summary() == null
                        ? ""
                        : preview.summary().strip();

        AiPreviewRiskLevel riskLevel =
                preview.riskLevel() == null
                        ? AiPreviewRiskLevel.UNKNOWN
                        : preview.riskLevel();

        List<String> keyIndicators =
                safeList(preview.keyIndicators());

        boolean sufficientInformation =
                Boolean.TRUE.equals(
                        preview.sufficientInformation()
                );

        List<String> limitations =
                safeList(preview.limitations());

        return new AiEvidencePreviewResponse(
                extraction.evidenceId(),
                extraction.caseId(),
                extraction.caseNumber(),
                extraction.originalFileName(),
                extraction.fileType(),
                summary,
                riskLevel,
                keyIndicators,
                sufficientInformation,
                limitations,
                true,
                Instant.now()
        );
    }

    private List<String> safeList(
            List<String> values
    ) {

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null)
                .map(String::strip)
                .filter(value -> !value.isBlank())
                .toList();
    }
}