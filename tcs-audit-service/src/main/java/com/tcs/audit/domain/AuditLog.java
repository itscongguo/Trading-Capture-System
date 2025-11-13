package com.tcs.audit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Audit log document for MongoDB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private String eventType;  // ORDER_CREATED, ORDER_UPDATED, TRADE_EXECUTED, etc.

    @Indexed
    private String entityType;  // ORDER, TRADE, USER, etc.

    @Indexed
    private String entityId;  // orderId, tradeId, userId, etc.

    @Indexed
    private String userId;

    private String traceId;

    private String action;  // CREATE, UPDATE, DELETE, EXECUTE

    private Map<String, Object> eventData;  // Full event payload

    @Indexed
    private Instant timestamp;

    private String source;  // Service name that produced the event
}
