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
 * Kafka consumer for order events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final AuditService auditService;

    @KafkaListener(topics = KafkaTopics.ORDERS, groupId = "audit-service-group")
    public void consumeOrderEvent(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            log.debug("Received order event for audit: {}", event);
            auditService.auditOrderEvent(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error auditing order event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.ORDER_STATUS, groupId = "audit-service-group")
    public void consumeOrderStatusEvent(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            log.debug("Received order status event for audit: {}", event);
            auditService.auditOrderStatusEvent(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error auditing order status event: {}", e.getMessage(), e);
        }
    }
}
