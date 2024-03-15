package com.tcs.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdatedEvent {
    private String orderId;
    private String userId;
    private String status;
    private String filledQuantity;
    private String avgPrice;
    private String rejectReason;
    private Long timestamp;
    private String traceId;
}
