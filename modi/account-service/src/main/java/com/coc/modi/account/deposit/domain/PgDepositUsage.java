package com.coc.modi.account.deposit.domain;

import com.coc.modi.account.wallet.domain.WalletTransaction;
import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "pg_deposit_usage", schema = "account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PgDepositUsage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_deposit_id", nullable = false)
    private PgDeposit pgDeposit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_transaction_id", nullable = false)
    private WalletTransaction walletTransaction;

    @Column(name = "used_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal usedAmount;

    private PgDepositUsage(Long memberId,
                           PgDeposit pgDeposit,
                           WalletTransaction walletTransaction,
                           BigDecimal usedAmount) {

        this.memberId = memberId;
        this.pgDeposit = pgDeposit;
        this.walletTransaction = walletTransaction;
        this.usedAmount = usedAmount;
    }

    public static PgDepositUsage create(Long memberId,
                                        PgDeposit pgDeposit,
                                        WalletTransaction walletTransaction,
                                        BigDecimal usedAmount) {

        return new PgDepositUsage(memberId, pgDeposit, walletTransaction, usedAmount);
    }
}
