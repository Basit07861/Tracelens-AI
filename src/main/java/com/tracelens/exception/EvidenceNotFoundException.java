package com.tracelens.exception;

public class EvidenceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EvidenceNotFoundException(String message) {
        super(message);
    }
}