package com.tcs.order.domain.entity;

import com.tcs.common.enums.OrderSide;
import com.tcs.common.enums.OrderStatus;
import com.tcs.common.enums.OrderType;
import com.tcs.common.enums.TimeInForce;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_created", columnList = "userId,createdAt"),
        @Index(name = "idx_symbol_status", columnList = "symbol,status,createdAt"),
        @Index(name = "idx_client_order", columnList = "clientOrderId")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(length = 64)
    private String clientOrderId;

    @Column(nullable = false, length = 36)
    private String userId;

    @Column(nullable = false, length = 36)
    private String accountId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderType type;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(precision = 20, scale = 8)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TimeInForce timeInForce;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(precision = 20, scale = 8)
    @Builder.Default
    private BigDecimal filledQuantity = BigDecimal.ZERO;

    @Column(precision = 20, scale = 8)
    private BigDecimal avgPrice;

    @Column(length = 255)
    private String rejectReason;

    @Column(length = 64)
    private String traceId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;
}
