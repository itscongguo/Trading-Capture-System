package com.tcs.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeExecutedEvent {
    private String tradeId;
    private String orderId;
    private String userId;
    private String symbol;
    private String side;
    private String quantity;
    private String price;
    private String totalAmount;
    private Long timestamp;
    private String traceId;
}
