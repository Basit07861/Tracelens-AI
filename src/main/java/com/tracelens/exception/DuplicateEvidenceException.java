package com.tracelens.exception;

public class DuplicateEvidenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateEvidenceException(String message) {
        super(message);
    }
}