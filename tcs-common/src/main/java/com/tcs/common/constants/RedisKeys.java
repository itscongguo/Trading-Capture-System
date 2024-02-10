package com.tcs.common.constants;

/**
 * Redis key pattern constants
 */
public final class RedisKeys {

    private RedisKeys() {
        // Prevent instantiation
    }

    public static final String ORDER_SUMMARY_PREFIX = "order:summary:";
    public static final String ORDER_LOCK_PREFIX = "lock:order:";
    public static final String USER_QUOTA_PREFIX = "quota:";
    public static final String SUBMIT_TIME_PREFIX = "submit_time:";
    public static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public static String orderSummary(String orderId) {
        return ORDER_SUMMARY_PREFIX + orderId;
    }

    public static String orderLock(String orderId) {
        return ORDER_LOCK_PREFIX + orderId;
    }

    public static String userQuota(String userId) {
        return USER_QUOTA_PREFIX + userId;
    }

    public static String submitTime(String userId) {
        return SUBMIT_TIME_PREFIX + userId;
    }

    public static String rateLimit(String identifier) {
        return RATE_LIMIT_PREFIX + identifier;
    }
}
