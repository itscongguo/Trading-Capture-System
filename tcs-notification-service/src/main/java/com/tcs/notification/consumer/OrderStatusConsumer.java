package com.tcs.notification.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.common.constants.KafkaTopics;
import com.tcs.notification.dto.OrderStatusNotification;
import com.tcs.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer for order status updates
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.ORDER_STATUS, groupId = "notification-service-group")
    public void consumeOrderStatus(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            log.debug("Received order status event: {}", event);

            OrderStatusNotification notification = OrderStatusNotification.builder()
                    .type("ORDER_STATUS")
                    .orderId((String) event.get("orderId"))
                    .userId((String) event.get("userId"))
                    .status((String) event.get("status"))
                    .filledQuantity((String) event.get("filledQuantity"))
                    .avgPrice((String) event.get("avgPrice"))
                    .rejectReason((String) event.get("rejectReason"))
                    .timestamp(getLongValue(event.get("timestamp")))
                    .build();

            notificationService.sendOrderStatusNotification(notification);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing order status event: {}", e.getMessage(), e);
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
