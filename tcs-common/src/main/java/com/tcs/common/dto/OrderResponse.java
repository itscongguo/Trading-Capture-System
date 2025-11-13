package com.tcs.common.dto;

import com.tcs.common.enums.OrderSide;
import com.tcs.common.enums.OrderStatus;
import com.tcs.common.enums.OrderType;
import com.tcs.common.enums.TimeInForce;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for order information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderId;
    private String clientOrderId;
    private String userId;
    private String accountId;
    private String symbol;
    private OrderSide side;
    private OrderType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private TimeInForce timeInForce;
    private OrderStatus status;
    private BigDecimal filledQuantity;
    private BigDecimal avgPrice;
    private String rejectReason;
    private Instant createdAt;
    private Instant updatedAt;
}
