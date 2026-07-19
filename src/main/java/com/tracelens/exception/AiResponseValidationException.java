package com.tracelens.exception;

public class AiResponseValidationException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AiResponseValidationException(
            String message
    ) {
        super(message);
    }

    public AiResponseValidationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}