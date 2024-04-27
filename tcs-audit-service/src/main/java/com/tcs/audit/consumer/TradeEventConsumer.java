package com.tcs.audit.consumer;

import com.tcs.audit.service.AuditService;
import com.tcs.common.constants.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer for trade events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventConsumer {

    private final AuditService auditService;

    @KafkaListener(topics = KafkaTopics.TRADES, groupId = "audit-service-group")
    public void consumeTradeEvent(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            log.debug("Received trade event for audit: {}", event);
            auditService.auditTradeEvent(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error auditing trade event: {}", e.getMessage(), e);
        }
    }
}
