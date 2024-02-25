package com.tcs.risk.controller;

import com.tcs.risk.dto.RiskCheckRequest;
import com.tcs.risk.dto.RiskCheckResponse;
import com.tcs.risk.service.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for risk operations
 */
@Slf4j
@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @PostMapping("/check")
    public ResponseEntity<RiskCheckResponse> checkRisk(@RequestBody RiskCheckRequest request) {
        log.info("Risk check request received for order {}", request.getOrderId());
        RiskCheckResponse response = riskService.checkRisk(request);
        return ResponseEntity.ok(response);
    }
}
