package com.tcs.order.service;

import com.tcs.common.constants.KafkaTopics;
import com.tcs.order.domain.entity.OrderEntity;
import com.tcs.order.service.dto.OrderCreatedEvent;
import com.tcs.order.service.dto.OrderUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for order events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderEntity order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getOrderId())
                .clientOrderId(order.getClientOrderId())
                .userId(order.getUserId())
                .accountId(order.getAccountId())
                .symbol(order.getSymbol())
                .side(order.getSide().name())
                .type(order.getType().name())
                .quantity(order.getQuantity().toString())
                .price(order.getPrice() != null ? order.getPrice().toString() : null)
                .timeInForce(order.getTimeInForce().name())
                .status(order.getStatus().name())
                .timestamp(order.getCreatedAt().toEpochMilli())
                .traceId(order.getTraceId())
                .build();

        sendEvent(KafkaTopics.ORDERS, order.getOrderId(), event);
    }

    public void publishOrderUpdated(OrderEntity order) {
        OrderUpdatedEvent event = OrderUpdatedEvent.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .filledQuantity(order.getFilledQuantity().toString())
                .avgPrice(order.getAvgPrice() != null ? order.getAvgPrice().toString() : null)
                .rejectReason(order.getRejectReason())
                .timestamp(order.getUpdatedAt().toEpochMilli())
                .traceId(order.getTraceId())
                .build();

        sendEvent(KafkaTopics.ORDER_STATUS, order.getOrderId(), event);
    }

    private void sendEvent(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event sent successfully to topic {} with key {}: offset={}",
                        topic, key, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send event to topic {} with key {}: {}",
                        topic, key, ex.getMessage(), ex);
            }
        });
    }
}
