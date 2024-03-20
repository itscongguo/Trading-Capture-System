package com.tcs.trade.service;

import com.tcs.common.util.IdGenerator;
import com.tcs.trade.domain.entity.TradeEntity;
import com.tcs.trade.domain.repository.TradeRepository;
import com.tcs.trade.dto.OrderCreatedEvent;
import com.tcs.trade.dto.OrderUpdatedEvent;
import com.tcs.trade.dto.TradeExecutedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;

/**
 * Simplified matching engine for demo purposes
 * In production, this would implement sophisticated matching algorithms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngine {

    private final TradeRepository tradeRepository;
    private final TradeEventProducer eventProducer;
    private final Random random = new Random();

    @Value("${app.matching.simulated-execution-probability}")
    private double executionProbability;

    /**
     * Process order and attempt matching
     */
    @Transactional
    public void processOrder(OrderCreatedEvent orderEvent) {
        log.info("Processing order {} for matching", orderEvent.getOrderId());

        // Skip if already rejected by risk
        if ("RISK_REJECTED".equals(orderEvent.getStatus())) {
            log.info("Order {} already rejected by risk, skipping matching", orderEvent.getOrderId());
            return;
        }

        // Simulate matching logic
        // In production, this would:
        // 1. Check order book for matching orders
        // 2. Apply price-time priority
        // 3. Execute matches
        // 4. Handle partial fills
        // 5. Manage order book state

        boolean shouldExecute = random.nextDouble() < executionProbability;

        if (shouldExecute) {
            executeOrder(orderEvent);
        } else {
            rejectOrder(orderEvent, "No matching orders available");
        }
    }

    private void executeOrder(OrderCreatedEvent orderEvent) {
        // For demo: execute at limit price for LIMIT orders, or simulate market price for MARKET
        BigDecimal executionPrice = orderEvent.getPrice() != null
                ? new BigDecimal(orderEvent.getPrice())
                : simulateMarketPrice();

        BigDecimal quantity = new BigDecimal(orderEvent.getQuantity());
        BigDecimal totalAmount = executionPrice.multiply(quantity);

        // Create trade record
        String tradeId = IdGenerator.generateTradeId();
        TradeEntity trade = TradeEntity.builder()
                .tradeId(tradeId)
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .symbol(orderEvent.getSymbol())
                .side(orderEvent.getSide())
                .quantity(quantity)
                .price(executionPrice)
                .totalAmount(totalAmount)
                .traceId(orderEvent.getTraceId())
                .build();

        trade = tradeRepository.save(trade);
        log.info("Trade {} executed: order={}, qty={}, price={}",
                tradeId, orderEvent.getOrderId(), quantity, executionPrice);

        // Publish trade executed event
        TradeExecutedEvent tradeEvent = TradeExecutedEvent.builder()
                .tradeId(tradeId)
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .symbol(orderEvent.getSymbol())
                .side(orderEvent.getSide())
                .quantity(quantity.toString())
                .price(executionPrice.toString())
                .totalAmount(totalAmount.toString())
                .timestamp(Instant.now().toEpochMilli())
                .traceId(orderEvent.getTraceId())
                .build();

        eventProducer.publishTradeExecuted(tradeEvent);

        // Publish order updated event (status = FILLED)
        OrderUpdatedEvent orderUpdate = OrderUpdatedEvent.builder()
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .status("FILLED")
                .filledQuantity(quantity.toString())
                .avgPrice(executionPrice.toString())
                .timestamp(Instant.now().toEpochMilli())
                .traceId(orderEvent.getTraceId())
                .build();

        eventProducer.publishOrderUpdated(orderUpdate);
    }

    private void rejectOrder(OrderCreatedEvent orderEvent, String reason) {
        log.info("Rejecting order {}: {}", orderEvent.getOrderId(), reason);

        OrderUpdatedEvent orderUpdate = OrderUpdatedEvent.builder()
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .status("REJECTED")
                .filledQuantity("0")
                .rejectReason(reason)
                .timestamp(Instant.now().toEpochMilli())
                .traceId(orderEvent.getTraceId())
                .build();

        eventProducer.publishOrderUpdated(orderUpdate);
    }

    private BigDecimal simulateMarketPrice() {
        // Simulate market price between 100 and 200
        double price = 100 + (random.nextDouble() * 100);
        return BigDecimal.valueOf(price).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
