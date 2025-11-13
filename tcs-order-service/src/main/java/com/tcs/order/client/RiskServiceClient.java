package com.tcs.order.client;

import com.tcs.order.client.dto.RiskCheckRequest;
import com.tcs.order.client.dto.RiskCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Risk Service
 */
@FeignClient(
        name = "risk-service",
        url = "${app.feign.risk-service.url}",
        fallbackFactory = RiskServiceFallbackFactory.class
)
public interface RiskServiceClient {

    @PostMapping("/api/risk/check")
    RiskCheckResponse checkRisk(@RequestBody RiskCheckRequest request);
}
