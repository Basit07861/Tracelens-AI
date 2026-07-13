package com.tracelens.exception;

import java.time.Instant;

public record ErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
}