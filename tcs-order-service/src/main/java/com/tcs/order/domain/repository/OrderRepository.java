package com.tcs.order.domain.repository;

import com.tcs.common.enums.OrderStatus;
import com.tcs.order.domain.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderId(String orderId);

    Optional<OrderEntity> findByClientOrderId(String clientOrderId);

    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    Page<OrderEntity> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    Page<OrderEntity> findByUserIdAndStatus(@Param("userId") String userId,
                                            @Param("status") OrderStatus status,
                                            Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.symbol = :symbol AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<OrderEntity> findBySymbolAndStatusIn(@Param("symbol") String symbol,
                                              @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.userId = :userId AND o.createdAt >= :since")
    long countByUserIdAndCreatedAtAfter(@Param("userId") String userId, @Param("since") Instant since);
}
