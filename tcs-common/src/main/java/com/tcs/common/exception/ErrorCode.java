package com.tcs.common.exception;

import lombok.Getter;

/**
 * Error code enumeration
 */
@Getter
public enum ErrorCode {

    // General errors (1xxx)
    INTERNAL_SERVER_ERROR("1000", "Internal server error"),
    INVALID_REQUEST("1001", "Invalid request"),
    RESOURCE_NOT_FOUND("1002", "Resource not found"),
    UNAUTHORIZED("1003", "Unauthorized"),
    FORBIDDEN("1004", "Forbidden"),
    RATE_LIMIT_EXCEEDED("1005", "Rate limit exceeded"),

    // Order errors (2xxx)
    ORDER_NOT_FOUND("2000", "Order not found"),
    DUPLICATE_ORDER("2001", "Duplicate order"),
    INVALID_ORDER_STATUS("2002", "Invalid order status"),
    INVALID_PRICE("2003", "Invalid price"),
    INVALID_QUANTITY("2004", "Invalid quantity"),
    INVALID_SYMBOL("2005", "Invalid symbol"),

    // Risk errors (3xxx)
    RISK_CHECK_FAILED("3000", "Risk check failed"),
    INSUFFICIENT_BALANCE("3001", "Insufficient balance"),
    POSITION_LIMIT_EXCEEDED("3002", "Position limit exceeded"),
    NOTIONAL_LIMIT_EXCEEDED("3003", "Notional limit exceeded"),
    ORDER_COUNT_LIMIT_EXCEEDED("3004", "Order count limit exceeded"),

    // Trade errors (4xxx)
    TRADE_EXECUTION_FAILED("4000", "Trade execution failed"),
    MATCHING_ENGINE_ERROR("4001", "Matching engine error"),

    // Infrastructure errors (5xxx)
    DATABASE_ERROR("5000", "Database error"),
    CACHE_ERROR("5001", "Cache error"),
    MESSAGE_QUEUE_ERROR("5002", "Message queue error");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
