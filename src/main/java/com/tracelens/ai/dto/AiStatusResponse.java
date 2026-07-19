package com.tracelens.ai.dto;

import java.time.Instant;

import com.tracelens.ai.entity.AiConnectionStatus;

public record AiStatusResponse(
        AiConnectionStatus status,
        String provider,
        String model,
        String message,
        Instant checkedAt
) {
}