package com.tracelens.ai.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tracelens.ai.dto.AiStatusResponse;
import com.tracelens.ai.entity.AiConnectionStatus;

@Service
public class AiStatusService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AiStatusService.class
            );

    private static final String EXPECTED_RESPONSE =
            "TRACELENS_AI_OK";

    private static final String CONNECTIVITY_PROMPT = """
            This is an application connectivity check.

            Respond with exactly this text and nothing else:

            TRACELENS_AI_OK
            """;

    private final ChatClient chatClient;
    private final String provider;
    private final String model;

    public AiStatusService(
            ChatClient traceLensChatClient,

            @Value("${app.ai.provider}")
            String provider,

            @Value("${spring.ai.openai.chat.model}")
            String model
    ) {
        this.chatClient = traceLensChatClient;
        this.provider = provider;
        this.model = model;
    }

    public AiStatusResponse checkStatus() {

        Instant checkedAt = Instant.now();

        try {
            String response = chatClient
                    .prompt()
                    .user(CONNECTIVITY_PROMPT)
                    .call()
                    .content();

            String normalizedResponse =
                    response == null
                            ? ""
                            : response.strip();

            if (
                    EXPECTED_RESPONSE.equals(
                            normalizedResponse
                    )
            ) {
                return new AiStatusResponse(
                        AiConnectionStatus.UP,
                        provider,
                        model,
                        "AI service is connected",
                        checkedAt
                );
            }

            LOGGER.warn(
                    "AI connectivity check returned an "
                    + "unexpected response format"
            );

            return new AiStatusResponse(
                    AiConnectionStatus.DOWN,
                    provider,
                    model,
                    "AI service returned an unexpected response",
                    checkedAt
            );
        }
        catch (RuntimeException exception) {

            LOGGER.error(
                    "AI connectivity check failed: {}",
                    exception.getClass().getSimpleName()
            );

            return new AiStatusResponse(
                    AiConnectionStatus.DOWN,
                    provider,
                    model,
                    "AI service is currently unavailable",
                    checkedAt
            );
        }
    }
}