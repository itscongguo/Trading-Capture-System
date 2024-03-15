package com.tcs.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String clientOrderId;
    private String userId;
    private String accountId;
    private String symbol;
    private String side;
    private String type;
    private String quantity;
    private String price;
    private String timeInForce;
    private String status;
    private Long timestamp;
    private String traceId;
}
