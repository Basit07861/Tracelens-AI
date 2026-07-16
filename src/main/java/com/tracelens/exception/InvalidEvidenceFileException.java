package com.tracelens.exception;

public class InvalidEvidenceFileException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidEvidenceFileException(String message) {
        super(message);
    }
}