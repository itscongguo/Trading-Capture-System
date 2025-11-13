package com.tcs.order.controller;

import com.tcs.common.dto.CreateOrderRequest;
import com.tcs.common.dto.OrderResponse;
import com.tcs.common.enums.OrderStatus;
import com.tcs.common.util.TraceContext;
import com.tcs.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for order operations
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        // Set trace ID for correlation
        if (traceId != null) {
            TraceContext.setTraceId(traceId);
        } else {
            TraceContext.setTraceId(TraceContext.generateTraceId());
        }

        // Set user ID from header (would normally come from JWT token)
        if (userId != null) {
            request.setUserId(userId);
        }

        log.info("Received order creation request for user {} with traceId {}",
                request.getUserId(), TraceContext.getTraceId());

        OrderResponse response = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        log.info("Fetching order: {}", orderId);
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {

        log.info("Fetching orders for user {} with status {}", userId, status);

        Page<OrderResponse> orders;
        if (status != null) {
            orders = orderService.getUserOrdersByStatus(userId, status, pageable);
        } else {
            orders = orderService.getUserOrders(userId, pageable);
        }

        return ResponseEntity.ok(orders);
    }
}
