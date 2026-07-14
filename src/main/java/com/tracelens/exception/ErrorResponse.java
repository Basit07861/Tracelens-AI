package com.tracelens.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors,
        Instant timestamp
) {
}