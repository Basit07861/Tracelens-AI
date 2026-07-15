package com.tracelens.exception;

public class CaseNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CaseNotFoundException(String message) {
        super(message);
    }
}