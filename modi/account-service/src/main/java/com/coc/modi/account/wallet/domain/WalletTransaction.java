package com.coc.modi.account.wallet.domain;

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

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 30)
    private WalletTransactionType txType;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "related_pg_deposit_id")
    private Long relatedPgDepositId;

    @Column(name = "related_rental_id")
    private Long relatedRentalId;

    @Column(name = "related_rental_item_id")
    private Long relatedRentalItemId;

    @Column(name = "related_settlement_id")
    private Long relatedSettlementId;

    @Column(length = 255)
    private String description;
	
	@Column(name = "payment_key", length = 100)
	private String paymentKey;

    public static WalletTransaction create(
            MemberWallet wallet,
            WalletTransactionType txType,
            BigDecimal amount,
            BigDecimal balanceAfter,
            Long relatedPgDepositId,
            Long relatedRentalId,
            Long relatedRentalItemId,
            Long relatedSettlementId,
            String description,
			String paymentKey
    ) {

        WalletTransaction tx = new WalletTransaction();

        tx.walletId = wallet.getId();
        tx.memberId = wallet.getMemberId();
        tx.txType = txType;
        tx.amount = amount;
        tx.balanceAfter = balanceAfter;
        tx.relatedPgDepositId = relatedPgDepositId;
        tx.relatedRentalId = relatedRentalId;
        tx.relatedRentalItemId = relatedRentalItemId;
        tx.relatedSettlementId = relatedSettlementId;
        tx.description = description;
		tx.paymentKey = paymentKey;

        return tx;
    }


}
