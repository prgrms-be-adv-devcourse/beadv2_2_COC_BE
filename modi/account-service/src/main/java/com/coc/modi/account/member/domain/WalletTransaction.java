package com.coc.modi.account.member.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "wallet_transaction", schema = "public")
public class WalletTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private MemberWallet wallet;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 30)
    private WalletTransactionType txType;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "related_pg_deposit_id")
    private Long relatedPgDepositId;

    @Column(name = "related_rental_id")
    private Long relatedRentalId;

    @Column(name = "related_settlement_id")
    private Long relatedSettlementId;

    @Column(name = "description", length = 255)
    private String description;

}
