package com.tcs.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.common.constants.KafkaTopics;
import com.tcs.notification.dto.TradeNotification;
import com.tcs.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer for trade executions
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradeConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.TRADES, groupId = "notification-service-group")
    public void consumeTrade(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            log.debug("Received trade event: {}", event);

            TradeNotification notification = TradeNotification.builder()
                    .type("TRADE_EXECUTED")
                    .tradeId((String) event.get("tradeId"))
                    .orderId((String) event.get("orderId"))
                    .userId((String) event.get("userId"))
                    .symbol((String) event.get("symbol"))
                    .side((String) event.get("side"))
                    .quantity((String) event.get("quantity"))
                    .price((String) event.get("price"))
                    .totalAmount((String) event.get("totalAmount"))
                    .timestamp(getLongValue(event.get("timestamp")))
                    .build();

            notificationService.sendTradeNotification(notification);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing trade event: {}", e.getMessage(), e);
            // Don't acknowledge - message will be redelivered
        }
    }

    private Long getLongValue(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return null;
    }
}
