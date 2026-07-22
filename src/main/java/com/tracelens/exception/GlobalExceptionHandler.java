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

    private static final HttpStatus
            EXTRACTION_FAILURE_STATUS =
                    HttpStatus.valueOf(422);

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

        ErrorResponse response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request,
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse>
            handleUnreadableRequestBody(

                    HttpMessageNotReadableException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request body is malformed or contains "
                + "unsupported values",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ErrorResponse>
            handleRequestParameterTypeMismatch(

                    MethodArgumentTypeMismatchException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid value for request parameter '"
                + exception.getName()
                + "'",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse>
            handleInvalidRequestException(

                    InvalidRequestException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(InvalidEvidenceFileException.class)
    public ResponseEntity<ErrorResponse>
            handleInvalidEvidenceFileException(

                    InvalidEvidenceFileException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(
            EvidenceTextExtractionException.class
    )
    public ResponseEntity<ErrorResponse>
            handleEvidenceTextExtractionException(

                    EvidenceTextExtractionException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                EXTRACTION_FAILURE_STATUS,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(EXTRACTION_FAILURE_STATUS)
                .body(response);
    }

    @ExceptionHandler(
            AiResponseValidationException.class
    )
    public ResponseEntity<ErrorResponse>
            handleAiResponseValidationException(

                    AiResponseValidationException exception,
                    HttpServletRequest request
            ) {

        LOGGER.warn(
                "AI response validation failed for request: {}",
                request.getRequestURI()
        );

        ErrorResponse response = createErrorResponse(
                HttpStatus.BAD_GATEWAY,
                "The AI service returned an invalid "
                + "structured response. Please try again.",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(response);
    }

    @ExceptionHandler(
            AiServiceUnavailableException.class
    )
    public ResponseEntity<ErrorResponse>
            handleAiServiceUnavailableException(

                    AiServiceUnavailableException exception,
                    HttpServletRequest request
            ) {

        String causeType =
                exception.getCause() == null
                        ? "Unknown"
                        : exception
                                .getCause()
                                .getClass()
                                .getSimpleName();

        LOGGER.error(
                "AI service failure for request {} "
                + "with error type {}",
                request.getRequestURI(),
                causeType
        );

        ErrorResponse response = createErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "The AI service is currently unavailable. "
                + "Please try again later.",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse>
            handleMaximumUploadSizeExceeded(

                    MaxUploadSizeExceededException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.CONTENT_TOO_LARGE,
                "Evidence file cannot exceed 10 MB",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body(response);
    }

    @ExceptionHandler(EvidenceStorageException.class)
    public ResponseEntity<ErrorResponse>
            handleEvidenceStorageException(

                    EvidenceStorageException exception,
                    HttpServletRequest request
            ) {

        LOGGER.error(
                "Evidence storage failure while "
                + "processing request: {}",
                request.getRequestURI()
        );

        ErrorResponse response = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "The evidence file could not be "
                + "processed. Please try again.",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse>
            handleDuplicateEmailException(

                    DuplicateEmailException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(DuplicateEvidenceException.class)
    public ResponseEntity<ErrorResponse>
            handleDuplicateEvidenceException(

                    DuplicateEvidenceException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
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

        ErrorResponse response = createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email address or password",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse>
            handleDisabledAccount(

                    DisabledException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.FORBIDDEN,
                "This user account is disabled",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse>
            handleUserNotFoundException(

                    UserNotFoundException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<ErrorResponse>
            handleCaseNotFoundException(

                    CaseNotFoundException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(EvidenceNotFoundException.class)
    public ResponseEntity<ErrorResponse>
            handleEvidenceNotFoundException(

                    EvidenceNotFoundException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /*
     * Missing and unowned notes return the same 404 response.
     * This prevents the API from revealing whether a note
     * belongs to another investigator.
     */
    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ErrorResponse>
            handleNoteNotFoundException(

                    NoteNotFoundException exception,
                    HttpServletRequest request
            ) {

        ErrorResponse response = createErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse>
            handleDataIntegrityViolation(

                    DataIntegrityViolationException exception,
                    HttpServletRequest request
            ) {

        LOGGER.warn(
                "Database constraint violation while "
                + "processing request: {}",
                request.getRequestURI()
        );

        ErrorResponse response = createErrorResponse(
                HttpStatus.CONFLICT,
                "The submitted information conflicts "
                + "with existing data",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
            handleGeneralException(

                    Exception exception,
                    HttpServletRequest request
            ) {

        LOGGER.error(
                "Unexpected error while processing "
                + "request {} with error type {}",
                request.getRequestURI(),
                exception
                        .getClass()
                        .getSimpleName()
        );

        ErrorResponse response = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. "
                + "Please try again.",
                request,
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
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