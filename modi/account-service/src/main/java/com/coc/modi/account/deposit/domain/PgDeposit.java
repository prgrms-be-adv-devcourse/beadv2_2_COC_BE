package com.coc.modi.account.deposit.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pg_deposit", schema = "public")
public class PgDeposit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PgDepositStatus status;

    @Column(name = "pg_provider", nullable = false, length = 50)
    private String pgProvider;

    @Column(name = "pg_tid", nullable = false, length = 100)
    private String pgTid;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "failed_reason", length = 255)
    private String failedReason;
}
