package com.tcs.trade.domain.repository;

import com.tcs.trade.domain.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    List<TradeEntity> findByOrderId(String orderId);

    @Query("SELECT t FROM TradeEntity t WHERE t.userId = :userId ORDER BY t.executedAt DESC")
    List<TradeEntity> findByUserId(@Param("userId") String userId);

    @Query("SELECT t FROM TradeEntity t WHERE t.symbol = :symbol ORDER BY t.executedAt DESC")
    List<TradeEntity> findBySymbol(@Param("symbol") String symbol);
}
