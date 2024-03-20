package com.tcs.trade.consumer;

import com.tcs.common.constants.KafkaTopics;
import com.tcs.trade.dto.OrderCreatedEvent;
import com.tcs.trade.service.MatchingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for order events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final MatchingEngine matchingEngine;

    @KafkaListener(topics = KafkaTopics.ORDERS, groupId = "trade-engine-group")
    public void consumeOrder(OrderCreatedEvent orderEvent, Acknowledgment acknowledgment) {
        try {
            log.info("Received order event: orderId={}, symbol={}, side={}",
                    orderEvent.getOrderId(), orderEvent.getSymbol(), orderEvent.getSide());

            matchingEngine.processOrder(orderEvent);

            // Manually acknowledge after successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing order {}: {}", orderEvent.getOrderId(), e.getMessage(), e);
            // Don't acknowledge - message will be redelivered
        }
    }
}
