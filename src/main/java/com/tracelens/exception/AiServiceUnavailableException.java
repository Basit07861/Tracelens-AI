package com.tracelens.exception;

public class AiServiceUnavailableException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AiServiceUnavailableException(
            String message
    ) {
        super(message);
    }

    public AiServiceUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}