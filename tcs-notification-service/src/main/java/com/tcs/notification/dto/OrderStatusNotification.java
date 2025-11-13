package com.tcs.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusNotification {
    private String type;  // ORDER_STATUS or TRADE_EXECUTED
    private String orderId;
    private String userId;
    private String status;
    private String filledQuantity;
    private String avgPrice;
    private String rejectReason;
    private Long timestamp;
}
