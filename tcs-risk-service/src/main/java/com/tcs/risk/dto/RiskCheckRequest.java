package com.tcs.risk.dto;

import com.tcs.common.enums.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskCheckRequest {
    private String orderId;
    private String userId;
    private String accountId;
    private String symbol;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
}
