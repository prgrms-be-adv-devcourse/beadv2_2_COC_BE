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

    // 충전 요청 생성
    public static PgDeposit createRequest(
            Long memberId,
            BigDecimal amount,
            String pgProvider,
            String orderId
    ) {
        PgDeposit pgDeposit = new PgDeposit();
        pgDeposit.memberId = memberId;
        pgDeposit.amount = amount;
        pgDeposit.pgProvider = pgProvider;
        pgDeposit.pgTid = orderId;
        pgDeposit.status = PgDepositStatus.REQUESTED;
        pgDeposit.requestedAt = LocalDateTime.now();

        return pgDeposit;
    }

    // 충전 승인 처리
    public void approve() {

        if(this.status != PgDepositStatus.REQUESTED){

            throw new IllegalStateException("REQEUSTED 상태만 승인 가능합니다. 현재 : " + this.status);
        }

        this.status = PgDepositStatus.SUCCESS;
        this.approvedAt = LocalDateTime.now();
    }

    // 충전 실패 처리
    public void fail(String failedReason) {

        if(this.status != PgDepositStatus.REQUESTED){

            throw new IllegalStateException("REQUESTED 상태만 실패처리 가능합니다. 현재 : " + this.status);
        }

        this.status = PgDepositStatus.FAILED;
        this.failedReason = failedReason;
    }



}
