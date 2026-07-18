package com.tracelens.exception;

public class EvidenceTextExtractionException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EvidenceTextExtractionException(
            String message
    ) {
        super(message);
    }

    public EvidenceTextExtractionException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}