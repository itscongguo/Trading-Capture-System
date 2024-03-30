package com.tcs.auth.controller;

import com.tcs.common.exception.ErrorCode;
import com.tcs.common.exception.ErrorResponse;
import com.tcs.common.exception.TcsException;
import com.tcs.common.util.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TcsException.class)
    public ResponseEntity<ErrorResponse> handleTcsException(TcsException ex) {
        log.error("TcsException: code={}, message={}, details={}",
                ex.getErrorCode().getCode(), ex.getMessage(), ex.getDetails());

        ErrorResponse response = ErrorResponse.from(ex);
        response.setTraceId(TraceContext.getTraceId());

        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));

        log.error("Validation error: {}", details);

        ErrorResponse response = ErrorResponse.from(ErrorCode.INVALID_REQUEST, details);
        response.setTraceId(TraceContext.getTraceId());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorResponse response = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
        response.setTraceId(TraceContext.getTraceId());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
