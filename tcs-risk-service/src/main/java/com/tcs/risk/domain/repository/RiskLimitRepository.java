package com.tcs.risk.domain.repository;

import com.tcs.risk.domain.entity.RiskLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskLimitRepository extends JpaRepository<RiskLimit, Long> {

    @Query("SELECT r FROM RiskLimit r WHERE r.userId = :userId AND r.accountId = :accountId AND r.symbol IS NULL AND r.enabled = true")
    Optional<RiskLimit> findAccountLimit(@Param("userId") String userId,
                                         @Param("accountId") String accountId);

    @Query("SELECT r FROM RiskLimit r WHERE r.userId = :userId AND r.accountId = :accountId AND r.symbol = :symbol AND r.enabled = true")
    Optional<RiskLimit> findSymbolLimit(@Param("userId") String userId,
                                       @Param("accountId") String accountId,
                                       @Param("symbol") String symbol);
}
