package com.tcs.trade.service;

import com.tcs.common.constants.KafkaTopics;
import com.tcs.trade.dto.OrderUpdatedEvent;
import com.tcs.trade.dto.TradeExecutedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer for trade events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTradeExecuted(TradeExecutedEvent event) {
        kafkaTemplate.send(KafkaTopics.TRADES, event.getTradeId(), event);
        log.info("Published trade executed event: tradeId={}", event.getTradeId());
    }

    public void publishOrderUpdated(OrderUpdatedEvent event) {
        kafkaTemplate.send(KafkaTopics.ORDER_STATUS, event.getOrderId(), event);
        log.info("Published order updated event: orderId={}, status={}",
                event.getOrderId(), event.getStatus());
    }
}
