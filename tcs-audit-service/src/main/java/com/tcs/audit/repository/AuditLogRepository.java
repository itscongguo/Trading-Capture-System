package com.tcs.audit.repository;

import com.tcs.audit.domain.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByUserId(String userId);

    List<AuditLog> findByEntityId(String entityId);

    List<AuditLog> findByEventType(String eventType);

    @Query("{'timestamp': {$gte: ?0, $lte: ?1}}")
    List<AuditLog> findByTimestampBetween(Instant start, Instant end);

    @Query("{'userId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<AuditLog> findByUserIdAndTimestampBetween(String userId, Instant start, Instant end);
}
