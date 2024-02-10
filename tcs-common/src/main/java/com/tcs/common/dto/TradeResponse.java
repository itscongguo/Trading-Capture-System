package com.tcs.common.dto;

import com.tcs.common.enums.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for trade execution information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeResponse {

    private String tradeId;
    private String orderId;
    private String symbol;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private Instant executedAt;
}
