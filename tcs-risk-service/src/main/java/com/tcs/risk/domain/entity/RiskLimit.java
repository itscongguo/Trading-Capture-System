package com.tcs.risk.domain.entity;

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
@Table(name = "risk_limits", indexes = {
        @Index(name = "idx_risk_user_account", columnList = "userId,accountId"),
        @Index(name = "idx_risk_user_symbol", columnList = "userId,symbol")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String userId;

    @Column(nullable = false, length = 36)
    private String accountId;

    @Column(length = 32)
    private String symbol;  // null means account-level limit

    @Column(precision = 20, scale = 2)
    private BigDecimal notionalLimit;  // Maximum notional value

    @Column(precision = 20, scale = 8)
    private BigDecimal positionLimit;  // Maximum position size

    @Column
    private Integer orderCountLimit;   // Maximum orders per day

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
