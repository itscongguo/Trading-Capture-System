package com.tcs.risk.service;

import com.tcs.common.enums.OrderSide;
import com.tcs.risk.domain.entity.RiskLimit;
import com.tcs.risk.domain.repository.RiskLimitRepository;
import com.tcs.risk.dto.RiskCheckRequest;
import com.tcs.risk.dto.RiskCheckResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Risk management service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskLimitRepository riskLimitRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.risk.default-notional-limit}")
    private BigDecimal defaultNotionalLimit;

    @Value("${app.risk.default-position-limit}")
    private BigDecimal defaultPositionLimit;

    @Value("${app.risk.default-order-count-limit}")
    private Integer defaultOrderCountLimit;

    @Value("${app.risk.quota-ttl}")
    private Long quotaTtl;

    /**
     * Perform risk check for an order
     */
    public RiskCheckResponse checkRisk(RiskCheckRequest request) {
        log.info("Performing risk check for order {} user {}", request.getOrderId(), request.getUserId());

        String riskDecisionId = UUID.randomUUID().toString();

        // Get risk limits (from DB or use defaults)
        RiskLimit accountLimit = riskLimitRepository
                .findAccountLimit(request.getUserId(), request.getAccountId())
                .orElse(createDefaultAccountLimit(request.getUserId(), request.getAccountId()));

        RiskLimit symbolLimit = riskLimitRepository
                .findSymbolLimit(request.getUserId(), request.getAccountId(), request.getSymbol())
                .orElse(null);

        // Calculate order notional value
        BigDecimal notionalValue = calculateNotionalValue(request);

        // Check notional limit using Redis
        BigDecimal effectiveNotionalLimit = symbolLimit != null && symbolLimit.getNotionalLimit() != null
                ? symbolLimit.getNotionalLimit()
                : accountLimit.getNotionalLimit();

        if (!checkNotionalLimit(request.getUserId(), notionalValue, effectiveNotionalLimit)) {
            log.warn("Notional limit exceeded for user {}", request.getUserId());
            return RiskCheckResponse.builder()
                    .approved(false)
                    .reason("Notional limit exceeded")
                    .riskDecisionId(riskDecisionId)
                    .build();
        }

        // Check position limit
        BigDecimal effectivePositionLimit = symbolLimit != null && symbolLimit.getPositionLimit() != null
                ? symbolLimit.getPositionLimit()
                : accountLimit.getPositionLimit();

        if (!checkPositionLimit(request.getUserId(), request.getSymbol(), request.getQuantity(), effectivePositionLimit)) {
            log.warn("Position limit exceeded for user {} symbol {}", request.getUserId(), request.getSymbol());
            return RiskCheckResponse.builder()
                    .approved(false)
                    .reason("Position limit exceeded")
                    .riskDecisionId(riskDecisionId)
                    .build();
        }

        // Check order count limit
        Integer effectiveOrderCountLimit = accountLimit.getOrderCountLimit();
        if (!checkOrderCountLimit(request.getUserId(), effectiveOrderCountLimit)) {
            log.warn("Order count limit exceeded for user {}", request.getUserId());
            return RiskCheckResponse.builder()
                    .approved(false)
                    .reason("Order count limit exceeded")
                    .riskDecisionId(riskDecisionId)
                    .build();
        }

        // Reserve quota in Redis
        reserveQuota(request.getUserId(), request.getSymbol(), notionalValue, request.getQuantity());

        log.info("Risk check passed for order {}", request.getOrderId());
        return RiskCheckResponse.builder()
                .approved(true)
                .reason("Risk check passed")
                .riskDecisionId(riskDecisionId)
                .build();
    }

    /**
     * Release reserved quota (called when order is cancelled or fails)
     */
    public void releaseQuota(String userId, String symbol, BigDecimal notionalValue, BigDecimal quantity) {
        log.info("Releasing quota for user {} symbol {}", userId, symbol);

        String notionalKey = "quota:notional:" + userId;
        String positionKey = "quota:position:" + userId + ":" + symbol;

        // Increment back the reserved amounts
        redisTemplate.opsForValue().increment(notionalKey, notionalValue.doubleValue());
        redisTemplate.opsForValue().increment(positionKey, quantity.doubleValue());
    }

    private BigDecimal calculateNotionalValue(RiskCheckRequest request) {
        if (request.getPrice() != null) {
            return request.getPrice().multiply(request.getQuantity());
        }
        // For market orders, use a conservative estimate (could be improved with real-time market data)
        return request.getQuantity().multiply(BigDecimal.valueOf(100)); // Placeholder
    }

    private boolean checkNotionalLimit(String userId, BigDecimal notionalValue, BigDecimal limit) {
        String key = "quota:notional:" + userId;

        // Get current usage
        String currentStr = redisTemplate.opsForValue().get(key);
        BigDecimal current = currentStr != null ? new BigDecimal(currentStr) : BigDecimal.ZERO;

        // Check if adding this order would exceed limit
        if (current.add(notionalValue).compareTo(limit) > 0) {
            return false;
        }

        return true;
    }

    private boolean checkPositionLimit(String userId, String symbol, BigDecimal quantity, BigDecimal limit) {
        String key = "quota:position:" + userId + ":" + symbol;

        String currentStr = redisTemplate.opsForValue().get(key);
        BigDecimal current = currentStr != null ? new BigDecimal(currentStr) : BigDecimal.ZERO;

        if (current.add(quantity).compareTo(limit) > 0) {
            return false;
        }

        return true;
    }

    private boolean checkOrderCountLimit(String userId, Integer limit) {
        String key = "quota:order_count:" + userId;

        String currentStr = redisTemplate.opsForValue().get(key);
        int current = currentStr != null ? Integer.parseInt(currentStr) : 0;

        return current < limit;
    }

    private void reserveQuota(String userId, String symbol, BigDecimal notionalValue, BigDecimal quantity) {
        // Reserve notional
        String notionalKey = "quota:notional:" + userId;
        redisTemplate.opsForValue().increment(notionalKey, notionalValue.doubleValue());
        redisTemplate.expire(notionalKey, quotaTtl, TimeUnit.SECONDS);

        // Reserve position
        String positionKey = "quota:position:" + userId + ":" + symbol;
        redisTemplate.opsForValue().increment(positionKey, quantity.doubleValue());
        redisTemplate.expire(positionKey, quotaTtl, TimeUnit.SECONDS);

        // Increment order count
        String countKey = "quota:order_count:" + userId;
        redisTemplate.opsForValue().increment(countKey, 1);
        redisTemplate.expire(countKey, quotaTtl, TimeUnit.SECONDS);
    }

    private RiskLimit createDefaultAccountLimit(String userId, String accountId) {
        return RiskLimit.builder()
                .userId(userId)
                .accountId(accountId)
                .notionalLimit(defaultNotionalLimit)
                .positionLimit(defaultPositionLimit)
                .orderCountLimit(defaultOrderCountLimit)
                .enabled(true)
                .build();
    }
}
