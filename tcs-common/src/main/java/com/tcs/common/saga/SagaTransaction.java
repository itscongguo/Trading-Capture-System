package com.tcs.common.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Saga Transaction - Represents a long-running distributed transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaTransaction {

    private String sagaId;
    private String transactionType;
    private SagaStatus status;
    private Map<String, Object> context = new HashMap<>();
    private Instant createdAt;
    private Instant updatedAt;
    private String currentStep;
    private int retryCount;
    private String errorMessage;

    public enum SagaStatus {
        STARTED,
        STEP_EXECUTING,
        STEP_COMPLETED,
        COMPENSATING,
        COMPENSATION_COMPLETED,
        COMPLETED,
        FAILED,
        ABORTED
    }

    public void addToContext(String key, Object value) {
        this.context.put(key, value);
    }

    public <T> T getFromContext(String key, Class<T> type) {
        Object value = this.context.get(key);
        return value != null ? type.cast(value) : null;
    }
}
