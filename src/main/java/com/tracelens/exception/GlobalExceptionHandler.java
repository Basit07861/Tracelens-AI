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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse>
            handleValidationException(

                    MethodArgumentNotValidException exception,
                    HttpServletRequest request
            ) {

        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        for (
                FieldError fieldError
                : exception
                        .getBindingResult()
                        .getFieldErrors()
        ) {

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

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request body is malformed or contains "
                + "unsupported values",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ErrorResponse>
            handleRequestParameterTypeMismatch(

                    MethodArgumentTypeMismatchException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid value for request parameter '"
                + exception.getName()
                + "'",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse>
            handleInvalidRequestException(

                    InvalidRequestException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(InvalidEvidenceFileException.class)
    public ResponseEntity<ErrorResponse>
            handleInvalidEvidenceFileException(

                    InvalidEvidenceFileException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(
            MaxUploadSizeExceededException.class
    )
    public ResponseEntity<ErrorResponse>
            handleMaximumUploadSizeExceeded(

                    MaxUploadSizeExceededException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONTENT_TOO_LARGE,
                "Evidence file cannot exceed 10 MB",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body(errorResponse);
    }

    @ExceptionHandler(EvidenceStorageException.class)
    public ResponseEntity<ErrorResponse>
            handleEvidenceStorageException(

                    EvidenceStorageException exception,
                    HttpServletRequest request
            ) {

        LOGGER.error(
                "Evidence storage failure while processing request: {}",
                request.getRequestURI(),
                exception
        );

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "The evidence file could not be stored. "
                + "Please try again.",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(errorResponse);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse>
            handleDuplicateEmailException(

                    DuplicateEmailException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                Map.of()
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

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email address or password",
                request,
                Map.of()
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

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN,
                "This user account is disabled",
                request,
                Map.of()
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

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                Map.of()
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

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(
            DataIntegrityViolationException.class
    )
    public ResponseEntity<ErrorResponse>
            handleDataIntegrityViolation(

                    DataIntegrityViolationException exception,
                    HttpServletRequest request
            ) {

        LOGGER.warn(
                "Database constraint violation while "
                + "processing request: {}",
                request.getRequestURI(),
                exception
        );

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.CONFLICT,
                "The submitted information conflicts "
                + "with existing data",
                request,
                Map.of()
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

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. "
                + "Please try again.",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(errorResponse);
    }

    private ErrorResponse createErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {

        return new ErrorResponse(
                false,
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors,
                Instant.now()
        );
    }
}