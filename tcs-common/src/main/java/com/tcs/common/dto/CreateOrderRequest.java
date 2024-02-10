package com.tcs.common.dto;

import com.tcs.common.enums.OrderSide;
import com.tcs.common.enums.OrderType;
import com.tcs.common.enums.TimeInForce;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    /**
     * Client-provided order ID for idempotency
     */
    private String clientOrderId;

    /**
     * Trading symbol (e.g., AAPL, MSFT)
     */
    @NotBlank(message = "Symbol is required")
    @Pattern(regexp = "^[A-Z]{1,10}$", message = "Symbol must be 1-10 uppercase letters")
    private String symbol;

    /**
     * Order side: BUY or SELL
     */
    @NotNull(message = "Side is required")
    private OrderSide side;

    /**
     * Order type: LIMIT or MARKET
     */
    @NotNull(message = "Order type is required")
    private OrderType type;

    /**
     * Order quantity
     */
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
    private BigDecimal quantity;

    /**
     * Price (required for LIMIT orders, null for MARKET)
     */
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    /**
     * Time in force
     */
    @NotNull(message = "Time in force is required")
    private TimeInForce timeInForce;

    /**
     * User ID (populated from JWT token)
     */
    private String userId;

    /**
     * Account ID
     */
    @NotBlank(message = "Account ID is required")
    private String accountId;
}
