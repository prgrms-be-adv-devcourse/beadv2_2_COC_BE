package com.coc.modi.account.withdrawal.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "withdrawal_request", schema = "account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "wallet_transaction_id", nullable = false)
    private Long walletTransactionId;

    @Column(name = "requested_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "fee_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "payout_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal payoutAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WithdrawalStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    private WithdrawalRequest(Long memberId,
                              Long walletTransactionId,
                              BigDecimal requestedAmount,
                              BigDecimal feeAmount,
                              BigDecimal payoutAmount) {

        this.memberId = memberId;
        this.walletTransactionId = walletTransactionId;
        this.requestedAmount = requestedAmount;
        this.feeAmount = feeAmount;
        this.payoutAmount = payoutAmount;
        this.status = WithdrawalStatus.REQUESTED;
    }

    public static WithdrawalRequest create(Long memberId,
                                           Long walletTransactionId,
                                           BigDecimal requestedAmount,
                                           BigDecimal feeAmount,
                                           BigDecimal payoutAmount) {

        if (requestedAmount == null || feeAmount == null || payoutAmount == null) {
            throw new IllegalArgumentException("출금 금액 정보가 올바르지 않습니다.");
        }
        BigDecimal expectedTotal = feeAmount.add(payoutAmount);
        if (requestedAmount.compareTo(expectedTotal) != 0) {
            throw new IllegalArgumentException("출금 요청 금액이 수수료 합계와 일치하지 않습니다.");
        }

        return new WithdrawalRequest(memberId, walletTransactionId, requestedAmount, feeAmount, payoutAmount);
    }

    public void markProcessing() {

        this.status = WithdrawalStatus.PROCESSING;
    }

    public void markCompleted() {

        this.status = WithdrawalStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {

        this.status = WithdrawalStatus.FAILED;
        this.processedAt = LocalDateTime.now();
        this.failureReason = reason;
    }
}
