package com.tcs.common.util;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ID generator utility for orders, trades, etc.
 */
public class IdGenerator {

    private IdGenerator() {
        // Prevent instantiation
    }

    /**
     * Generate unique order ID
     * Format: ORD-{timestamp}-{random}
     */
    public static String generateOrderId() {
        long timestamp = Instant.now().toEpochMilli();
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return String.format("ORD-%d-%d", timestamp, random);
    }

    /**
     * Generate unique trade ID
     * Format: TRD-{timestamp}-{random}
     */
    public static String generateTradeId() {
        long timestamp = Instant.now().toEpochMilli();
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return String.format("TRD-%d-%d", timestamp, random);
    }

    /**
     * Generate UUID-based ID
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }
}
