package com.tcs.order.client;

import com.tcs.order.client.dto.RiskCheckRequest;
import com.tcs.order.client.dto.RiskCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback factory for Risk Service circuit breaker
 */
@Slf4j
@Component
public class RiskServiceFallbackFactory implements FallbackFactory<RiskServiceClient> {

    @Override
    public RiskServiceClient create(Throwable cause) {
        return new RiskServiceClient() {
            @Override
            public RiskCheckResponse checkRisk(RiskCheckRequest request) {
                log.error("Risk service call failed for order {}: {}",
                         request.getOrderId(), cause.getMessage());
                return RiskCheckResponse.builder()
                        .approved(false)
                        .reason("Risk service unavailable")
                        .build();
            }
        };
    }
}
