package com.tracelens.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError
                : exception
                        .getBindingResult()
                        .getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request validation failed",
                request.getRequestURI(),
                fieldErrors,
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse>
            handleUnreadableRequestBody(
                    HttpMessageNotReadableException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request body is malformed or contains unsupported values",
                request.getRequestURI(),
                Map.of(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse>
            handleDuplicateEmailException(
                    DuplicateEmailException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ErrorResponse>
            handleInvalidCredentials(
                    RuntimeException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid email address or password",
                request.getRequestURI(),
                Map.of(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse>
            handleDisabledAccount(
                    DisabledException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "This user account is disabled",
                request.getRequestURI(),
                Map.of(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse>
            handleUserNotFoundException(
                    UserNotFoundException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<ErrorResponse>
            handleCaseNotFoundException(
                    CaseNotFoundException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse>
            handleDataIntegrityViolation(
                    DataIntegrityViolationException exception,
                    HttpServletRequest request
            ) {

        LOGGER.warn(
                "Database constraint violation while processing request: {}",
                request.getRequestURI(),
                exception
        );

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "The submitted information conflicts with existing data",
                request.getRequestURI(),
                Map.of(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
            handleGeneralException(
                    Exception exception,
                    HttpServletRequest request
            ) {

        LOGGER.error(
                "Unexpected error while processing request: {}",
                request.getRequestURI(),
                exception
        );

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR
                        .getReasonPhrase(),
                "An unexpected error occurred. Please try again.",
                request.getRequestURI(),
                Map.of(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}