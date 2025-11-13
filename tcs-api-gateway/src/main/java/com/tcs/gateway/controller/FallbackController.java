package com.tcs.gateway.controller;

import com.tcs.common.exception.ErrorCode;
import com.tcs.common.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fallback controller for circuit breaker
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<ErrorResponse> authFallback() {
        log.error("Auth service is unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Auth service is temporarily unavailable. Please try again later."));
    }

    @GetMapping("/order")
    public ResponseEntity<ErrorResponse> orderFallback() {
        log.error("Order service is unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Order service is temporarily unavailable. Please try again later."));
    }
}
