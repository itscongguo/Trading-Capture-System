package com.tcs.common.enums;

/**
 * Order status enumeration
 */
public enum OrderStatus {
    PENDING,            // Order received, pending processing
    RISK_CHECKING,      // Risk check in progress
    RISK_REJECTED,      // Rejected by risk service
    SUBMITTED,          // Submitted to trade engine
    PARTIALLY_FILLED,   // Partially executed
    FILLED,             // Fully executed
    CANCELLED,          // Cancelled by user or system
    REJECTED,           // Rejected by trade engine
    EXPIRED             // Expired (for time-based orders)
}
