package com.tcs.trade.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades", indexes = {
        @Index(name = "idx_trades_order", columnList = "orderId"),
        @Index(name = "idx_trades_user", columnList = "userId,executedAt"),
        @Index(name = "idx_trades_symbol", columnList = "symbol,executedAt")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tradeId;

    @Column(nullable = false, length = 64)
    private String orderId;

    @Column(nullable = false, length = 36)
    private String userId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 4)
    private String side;  // BUY or SELL

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 64)
    private String traceId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant executedAt;
}
