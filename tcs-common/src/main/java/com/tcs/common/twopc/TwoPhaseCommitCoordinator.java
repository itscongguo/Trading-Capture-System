package com.tcs.common.twopc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * Two-Phase Commit Coordinator
 * For strongly consistent short transactions with <0.2% rollback rate
 */
@Slf4j
@Component
public class TwoPhaseCommitCoordinator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ExecutorService executorService;
    private final Map<String, TransactionContext> activeTransactions = new ConcurrentHashMap<>();

    public TwoPhaseCommitCoordinator(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2);
    }

    /**
     * Execute 2PC transaction across multiple participants
     */
    public <T> T execute(String transactionId, List<TransactionParticipant<T>> participants, T data) {
        TransactionContext context = new TransactionContext(transactionId, participants.size());
        activeTransactions.put(transactionId, context);

        try {
            // Acquire distributed lock
            if (!acquireLock(transactionId)) {
                throw new TwoPhaseCommitException("Failed to acquire distributed lock");
            }

            // Phase 1: Prepare
            log.info("2PC Phase 1 (Prepare): transactionId={}, participants={}",
                transactionId, participants.size());

            boolean prepareSuccess = preparePhase(transactionId, participants, data, context);

            if (!prepareSuccess) {
                log.warn("2PC Prepare phase failed: transactionId={}", transactionId);
                rollbackPhase(transactionId, participants, data, context);
                throw new TwoPhaseCommitException("Prepare phase failed, transaction rolled back");
            }

            // Phase 2: Commit
            log.info("2PC Phase 2 (Commit): transactionId={}", transactionId);

            boolean commitSuccess = commitPhase(transactionId, participants, data, context);

            if (!commitSuccess) {
                log.error("2PC Commit phase failed: transactionId={}", transactionId);
                // Attempt rollback even after commit started
                rollbackPhase(transactionId, participants, data, context);
                throw new TwoPhaseCommitException("Commit phase failed");
            }

            context.setStatus(TransactionStatus.COMMITTED);
            log.info("2PC transaction committed successfully: transactionId={}", transactionId);

            return data;

        } catch (Exception e) {
            context.setStatus(TransactionStatus.ABORTED);
            log.error("2PC transaction failed: transactionId={}, error={}", transactionId, e.getMessage());
            throw new TwoPhaseCommitException("Transaction failed: " + e.getMessage(), e);
        } finally {
            releaseLock(transactionId);
            activeTransactions.remove(transactionId);
        }
    }

    private <T> boolean preparePhase(String transactionId,
                                      List<TransactionParticipant<T>> participants,
                                      T data,
                                      TransactionContext context) {
        context.setStatus(TransactionStatus.PREPARING);

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (TransactionParticipant<T> participant : participants) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    log.debug("Preparing participant: transactionId={}, participant={}",
                        transactionId, participant.getName());

                    boolean prepared = participant.prepare(transactionId, data);

                    if (prepared) {
                        context.recordPrepared(participant.getName());
                        log.debug("Participant prepared: transactionId={}, participant={}",
                            transactionId, participant.getName());
                    } else {
                        log.warn("Participant failed to prepare: transactionId={}, participant={}",
                            transactionId, participant.getName());
                    }

                    return prepared;
                } catch (Exception e) {
                    log.error("Participant prepare error: transactionId={}, participant={}, error={}",
                        transactionId, participant.getName(), e.getMessage());
                    return false;
                }
            }, executorService);

            futures.add(future);
        }

        try {
            // Wait for all participants with timeout
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
            allOf.get(5, TimeUnit.SECONDS);

            // Check if all succeeded
            boolean allPrepared = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .allMatch(Boolean::booleanValue);

            context.setStatus(allPrepared ? TransactionStatus.PREPARED : TransactionStatus.PREPARE_FAILED);
            return allPrepared;

        } catch (TimeoutException e) {
            log.error("Prepare phase timeout: transactionId={}", transactionId);
            context.setStatus(TransactionStatus.PREPARE_FAILED);
            return false;
        } catch (Exception e) {
            log.error("Prepare phase error: transactionId={}, error={}", transactionId, e.getMessage());
            context.setStatus(TransactionStatus.PREPARE_FAILED);
            return false;
        }
    }

    private <T> boolean commitPhase(String transactionId,
                                     List<TransactionParticipant<T>> participants,
                                     T data,
                                     TransactionContext context) {
        context.setStatus(TransactionStatus.COMMITTING);

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (TransactionParticipant<T> participant : participants) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    log.debug("Committing participant: transactionId={}, participant={}",
                        transactionId, participant.getName());

                    boolean committed = participant.commit(transactionId, data);

                    if (committed) {
                        context.recordCommitted(participant.getName());
                        log.debug("Participant committed: transactionId={}, participant={}",
                            transactionId, participant.getName());
                    }

                    return committed;
                } catch (Exception e) {
                    log.error("Participant commit error: transactionId={}, participant={}, error={}",
                        transactionId, participant.getName(), e.getMessage());
                    return false;
                }
            }, executorService);

            futures.add(future);
        }

        try {
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
            allOf.get(10, TimeUnit.SECONDS);

            boolean allCommitted = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .allMatch(Boolean::booleanValue);

            return allCommitted;

        } catch (Exception e) {
            log.error("Commit phase error: transactionId={}, error={}", transactionId, e.getMessage());
            return false;
        }
    }

    private <T> void rollbackPhase(String transactionId,
                                    List<TransactionParticipant<T>> participants,
                                    T data,
                                    TransactionContext context) {
        context.setStatus(TransactionStatus.ROLLING_BACK);

        log.info("Rolling back transaction: transactionId={}", transactionId);

        for (TransactionParticipant<T> participant : participants) {
            try {
                if (context.isPrepared(participant.getName())) {
                    log.debug("Rolling back participant: transactionId={}, participant={}",
                        transactionId, participant.getName());

                    participant.rollback(transactionId, data);

                    log.debug("Participant rolled back: transactionId={}, participant={}",
                        transactionId, participant.getName());
                }
            } catch (Exception e) {
                log.error("Rollback error: transactionId={}, participant={}, error={}",
                    transactionId, participant.getName(), e.getMessage());
            }
        }

        context.setStatus(TransactionStatus.ROLLED_BACK);
    }

    private boolean acquireLock(String transactionId) {
        String lockKey = "2pc:lock:" + transactionId;
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "locked", Duration.ofSeconds(30));
        return Boolean.TRUE.equals(acquired);
    }

    private void releaseLock(String transactionId) {
        String lockKey = "2pc:lock:" + transactionId;
        redisTemplate.delete(lockKey);
    }

    public TransactionContext getTransactionStatus(String transactionId) {
        return activeTransactions.get(transactionId);
    }

    public interface TransactionParticipant<T> {
        String getName();
        boolean prepare(String transactionId, T data);
        boolean commit(String transactionId, T data);
        void rollback(String transactionId, T data);
    }

    public enum TransactionStatus {
        PREPARING,
        PREPARED,
        PREPARE_FAILED,
        COMMITTING,
        COMMITTED,
        ROLLING_BACK,
        ROLLED_BACK,
        ABORTED
    }

    public static class TransactionContext {
        private final String transactionId;
        private final int participantCount;
        private final Set<String> preparedParticipants = ConcurrentHashMap.newKeySet();
        private final Set<String> committedParticipants = ConcurrentHashMap.newKeySet();
        private TransactionStatus status;

        public TransactionContext(String transactionId, int participantCount) {
            this.transactionId = transactionId;
            this.participantCount = participantCount;
            this.status = TransactionStatus.PREPARING;
        }

        public void recordPrepared(String participantName) {
            preparedParticipants.add(participantName);
        }

        public void recordCommitted(String participantName) {
            committedParticipants.add(participantName);
        }

        public boolean isPrepared(String participantName) {
            return preparedParticipants.contains(participantName);
        }

        public TransactionStatus getStatus() {
            return status;
        }

        public void setStatus(TransactionStatus status) {
            this.status = status;
        }

        public int getPreparedCount() {
            return preparedParticipants.size();
        }

        public int getCommittedCount() {
            return committedParticipants.size();
        }
    }

    public static class TwoPhaseCommitException extends RuntimeException {
        public TwoPhaseCommitException(String message) {
            super(message);
        }

        public TwoPhaseCommitException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
