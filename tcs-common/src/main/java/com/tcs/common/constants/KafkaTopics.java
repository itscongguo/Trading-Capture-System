package com.tcs.common.constants;

/**
 * Kafka topic name constants
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Prevent instantiation
    }

    public static final String ORDERS = "orders";
    public static final String ORDER_STATUS = "order-status";
    public static final String TRADES = "trades";
    public static final String RISK_EVENTS = "risk-events";
    public static final String AUDIT_EVENTS = "audit-events";
}
