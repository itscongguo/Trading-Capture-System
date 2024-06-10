package com.tcs.common.saga;

import java.util.function.BiFunction;

/**
 * Saga Step - Represents a single step in a Saga transaction with compensation logic
 */
public class SagaStep<T, R> {

    private final String stepName;
    private final BiFunction<SagaTransaction, T, R> action;
    private final BiFunction<SagaTransaction, R, Boolean> compensation;
    private final int maxRetries;

    public SagaStep(String stepName,
                    BiFunction<SagaTransaction, T, R> action,
                    BiFunction<SagaTransaction, R, Boolean> compensation) {
        this(stepName, action, compensation, 3);
    }

    public SagaStep(String stepName,
                    BiFunction<SagaTransaction, T, R> action,
                    BiFunction<SagaTransaction, R, Boolean> compensation,
                    int maxRetries) {
        this.stepName = stepName;
        this.action = action;
        this.compensation = compensation;
        this.maxRetries = maxRetries;
    }

    public String getStepName() {
        return stepName;
    }

    public R execute(SagaTransaction saga, T input) {
        return action.apply(saga, input);
    }

    public boolean compensate(SagaTransaction saga, R actionResult) {
        return compensation.apply(saga, actionResult);
    }

    public int getMaxRetries() {
        return maxRetries;
    }
}
