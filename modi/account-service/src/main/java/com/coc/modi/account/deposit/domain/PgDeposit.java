package com.coc.modi.account.deposit.domain;

import com.coc.modi.account.wallet.exception.AccountException;
import com.coc.modi.common.BaseEntity;
import com.coc.modi.common.ErrorCode;

import jakarta.persistence.*;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "pg_deposit",
        schema = "account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pg_deposit_payment_key", columnNames = "payment_key")
        }
)
public class PgDeposit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "remaining_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PgDepositStatus status;

    @Column(name = "pg_provider", nullable = false, length = 50)
    private String pgProvider;

    @Column(name = "pg_tid", nullable = false, length = 100)
    private String pgTid;

    @Column(name = "payment_key", length = 100)
    private String paymentKey;

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
        pgDeposit.remainingAmount = amount;
        pgDeposit.pgProvider = pgProvider;
        pgDeposit.pgTid = orderId;
        pgDeposit.status = PgDepositStatus.REQUESTED;
        pgDeposit.requestedAt = LocalDateTime.now();

        return pgDeposit;
    }

    // 충전 승인 처리
    public void approve(String paymentKey) {

        if (this.status != PgDepositStatus.REQUESTED) {

            throw new AccountException(ErrorCode.CONFLICT, "REQUESTED 상태만 승인 가능합니다. 현재 : " + this.status);
        }

        this.paymentKey = paymentKey;
        this.status = PgDepositStatus.SUCCESS;
        this.approvedAt = LocalDateTime.now();
    }

    // 충전 실패 처리
    public void fail(String failedReason) {

        if (this.status != PgDepositStatus.REQUESTED) {

            throw new AccountException(ErrorCode.CONFLICT, "REQUESTED 상태만 실패처리 가능합니다. 현재 : " + this.status);
        }

        this.status = PgDepositStatus.FAILED;
        this.failedReason = failedReason;
    }

    // 취소 가능 여부
    public boolean isCancelable() {

        return this.status == PgDepositStatus.REQUESTED || this.status == PgDepositStatus.SUCCESS;
    }

    // 결제 취소(환불)
    public void cancel(String reason) {

        if (this.status != PgDepositStatus.SUCCESS && this.status != PgDepositStatus.REQUESTED) {

            throw new AccountException(ErrorCode.CONFLICT, "SUCCESS 상태만 취소할 수 있습니다. : " + this.status);
        }

        this.status = PgDepositStatus.CANCELED;
        this.failedReason = reason;
        this.remainingAmount = BigDecimal.ZERO;
    }

    public boolean isUnused() {

        if (amount == null || remainingAmount == null) {
            return true;
        }
        return remainingAmount.compareTo(amount) == 0;
    }

    public void allocate(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException(ErrorCode.INVALID_INPUT, "할당 금액은 0보다 커야 합니다.");
        }
        if (remainingAmount == null || remainingAmount.compareTo(amount) < 0) {
            throw new AccountException(ErrorCode.CONFLICT, "충전 잔액이 부족합니다.");
        }
        this.remainingAmount = this.remainingAmount.subtract(amount);
    }
}
