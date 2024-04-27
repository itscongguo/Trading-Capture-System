package com.tcs.audit.service;

import com.tcs.audit.domain.AuditLog;
import com.tcs.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for audit logging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Audit order creation event
     */
    public void auditOrderEvent(Map<String, Object> event) {
        AuditLog auditLog = AuditLog.builder()
                .eventType("ORDER_CREATED")
                .entityType("ORDER")
                .entityId((String) event.get("orderId"))
                .userId((String) event.get("userId"))
                .traceId((String) event.get("traceId"))
                .action("CREATE")
                .eventData(new HashMap<>(event))
                .timestamp(Instant.ofEpochMilli(getLongValue(event.get("timestamp"))))
                .source("order-service")
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log created for order: {}", event.get("orderId"));
    }

    /**
     * Audit order status update event
     */
    public void auditOrderStatusEvent(Map<String, Object> event) {
        AuditLog auditLog = AuditLog.builder()
                .eventType("ORDER_STATUS_UPDATED")
                .entityType("ORDER")
                .entityId((String) event.get("orderId"))
                .userId((String) event.get("userId"))
                .traceId((String) event.get("traceId"))
                .action("UPDATE")
                .eventData(new HashMap<>(event))
                .timestamp(Instant.ofEpochMilli(getLongValue(event.get("timestamp"))))
                .source("trade-engine")
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log created for order status update: {}", event.get("orderId"));
    }

    /**
     * Audit trade execution event
     */
    public void auditTradeEvent(Map<String, Object> event) {
        AuditLog auditLog = AuditLog.builder()
                .eventType("TRADE_EXECUTED")
                .entityType("TRADE")
                .entityId((String) event.get("tradeId"))
                .userId((String) event.get("userId"))
                .traceId((String) event.get("traceId"))
                .action("EXECUTE")
                .eventData(new HashMap<>(event))
                .timestamp(Instant.ofEpochMilli(getLongValue(event.get("timestamp"))))
                .source("trade-engine")
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log created for trade execution: {}", event.get("tradeId"));
    }

    private Long getLongValue(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return Instant.now().toEpochMilli();
    }
}
