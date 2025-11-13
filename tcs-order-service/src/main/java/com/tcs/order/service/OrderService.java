package com.tcs.order.service;

import com.tcs.common.dto.CreateOrderRequest;
import com.tcs.common.dto.OrderResponse;
import com.tcs.common.enums.OrderStatus;
import com.tcs.common.enums.OrderType;
import com.tcs.common.exception.ErrorCode;
import com.tcs.common.exception.TcsException;
import com.tcs.common.util.IdGenerator;
import com.tcs.common.util.TraceContext;
import com.tcs.order.client.RiskServiceClient;
import com.tcs.order.client.dto.RiskCheckRequest;
import com.tcs.order.client.dto.RiskCheckResponse;
import com.tcs.order.domain.entity.OrderEntity;
import com.tcs.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Order service business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;
    private final RiskServiceClient riskServiceClient;
    private final RedissonClient redissonClient;

    /**
     * Create a new order
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String traceId = TraceContext.getTraceId();
        log.info("Creating order for user {} with traceId {}", request.getUserId(), traceId);

        // Validate request
        validateOrderRequest(request);

        // Check for idempotency
        if (request.getClientOrderId() != null) {
            OrderEntity existingOrder = orderRepository
                    .findByClientOrderId(request.getClientOrderId())
                    .orElse(null);
            if (existingOrder != null) {
                log.info("Duplicate order detected: clientOrderId={}", request.getClientOrderId());
                return mapToResponse(existingOrder);
            }
        }

        // Generate order ID
        String orderId = IdGenerator.generateOrderId();

        // Acquire distributed lock to prevent double submission
        RLock lock = redissonClient.getLock("lock:order:" + orderId);
        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new TcsException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to acquire lock");
            }

            // Perform risk check
            RiskCheckResponse riskCheck = performRiskCheck(orderId, request);
            if (!riskCheck.isApproved()) {
                log.warn("Risk check failed for order {}: {}", orderId, riskCheck.getReason());
                return createRejectedOrder(orderId, request, riskCheck.getReason(), traceId);
            }

            // Create order entity
            OrderEntity order = OrderEntity.builder()
                    .orderId(orderId)
                    .clientOrderId(request.getClientOrderId())
                    .userId(request.getUserId())
                    .accountId(request.getAccountId())
                    .symbol(request.getSymbol())
                    .side(request.getSide())
                    .type(request.getType())
                    .quantity(request.getQuantity())
                    .price(request.getPrice())
                    .timeInForce(request.getTimeInForce())
                    .status(OrderStatus.PENDING)
                    .filledQuantity(BigDecimal.ZERO)
                    .traceId(traceId)
                    .build();

            // Save to database
            order = orderRepository.save(order);
            log.info("Order {} created successfully with status {}", orderId, order.getStatus());

            // Publish to Kafka
            eventProducer.publishOrderCreated(order);

            return mapToResponse(order);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TcsException(ErrorCode.INTERNAL_SERVER_ERROR, "Order creation interrupted", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * Get order by ID
     */
    public OrderResponse getOrder(String orderId) {
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TcsException(ErrorCode.ORDER_NOT_FOUND, orderId));
        return mapToResponse(order);
    }

    /**
     * Get orders for a user
     */
    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get user orders by status
     */
    public Page<OrderResponse> getUserOrdersByStatus(String userId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Update order status (called by Kafka consumers or internal services)
     */
    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus status, String rejectReason) {
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TcsException(ErrorCode.ORDER_NOT_FOUND, orderId));

        order.setStatus(status);
        if (rejectReason != null) {
            order.setRejectReason(rejectReason);
        }

        order = orderRepository.save(order);
        eventProducer.publishOrderUpdated(order);

        log.info("Order {} status updated to {}", orderId, status);
    }

    private void validateOrderRequest(CreateOrderRequest request) {
        // Validate limit order has price
        if (request.getType() == OrderType.LIMIT && request.getPrice() == null) {
            throw new TcsException(ErrorCode.INVALID_PRICE, "Limit order must have price");
        }

        // Validate quantity is positive
        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TcsException(ErrorCode.INVALID_QUANTITY, "Quantity must be positive");
        }

        // Validate price is positive for limit orders
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TcsException(ErrorCode.INVALID_PRICE, "Price must be positive");
        }
    }

    private RiskCheckResponse performRiskCheck(String orderId, CreateOrderRequest request) {
        try {
            RiskCheckRequest riskRequest = RiskCheckRequest.builder()
                    .orderId(orderId)
                    .userId(request.getUserId())
                    .accountId(request.getAccountId())
                    .symbol(request.getSymbol())
                    .side(request.getSide())
                    .quantity(request.getQuantity())
                    .price(request.getPrice())
                    .build();

            return riskServiceClient.checkRisk(riskRequest);
        } catch (Exception e) {
            log.error("Risk check failed for order {}: {}", orderId, e.getMessage());
            return RiskCheckResponse.builder()
                    .approved(false)
                    .reason("Risk service error: " + e.getMessage())
                    .build();
        }
    }

    private OrderResponse createRejectedOrder(String orderId, CreateOrderRequest request,
                                              String rejectReason, String traceId) {
        OrderEntity order = OrderEntity.builder()
                .orderId(orderId)
                .clientOrderId(request.getClientOrderId())
                .userId(request.getUserId())
                .accountId(request.getAccountId())
                .symbol(request.getSymbol())
                .side(request.getSide())
                .type(request.getType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .timeInForce(request.getTimeInForce())
                .status(OrderStatus.RISK_REJECTED)
                .filledQuantity(BigDecimal.ZERO)
                .rejectReason(rejectReason)
                .traceId(traceId)
                .build();

        order = orderRepository.save(order);
        eventProducer.publishOrderCreated(order);

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(OrderEntity order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .clientOrderId(order.getClientOrderId())
                .userId(order.getUserId())
                .accountId(order.getAccountId())
                .symbol(order.getSymbol())
                .side(order.getSide())
                .type(order.getType())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .timeInForce(order.getTimeInForce())
                .status(order.getStatus())
                .filledQuantity(order.getFilledQuantity())
                .avgPrice(order.getAvgPrice())
                .rejectReason(order.getRejectReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
