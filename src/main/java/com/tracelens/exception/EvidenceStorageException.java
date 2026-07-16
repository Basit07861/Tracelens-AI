package com.tracelens.exception;

public class EvidenceStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EvidenceStorageException(String message) {
        super(message);
    }

    public EvidenceStorageException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}