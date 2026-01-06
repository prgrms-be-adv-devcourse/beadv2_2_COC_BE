package com.coc.modi.account.wallet.application.dto;

import com.coc.modi.account.wallet.domain.WalletTransaction;
import com.coc.modi.account.wallet.domain.WalletTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionResponse(
		
        WalletTransactionType txType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Long relatedRentalId,
        Long relatedSettlementId,
        String description,
        LocalDateTime createdAt,
		String paymentKey,
		String pgTid
) {

    public static WalletTransactionResponse from(WalletTransaction tx) {
		
		var pgDeposit = tx.getPgDeposit(); // null일 수 있음
		
		String pgTid = pgDeposit != null ? pgDeposit.getPgTid() : null;
		
        return new WalletTransactionResponse(
                tx.getTxType(),
                tx.getAmount(),
                tx.getBalanceAfter(),
                tx.getRelatedRentalId(),
                tx.getRelatedSettlementId(),
                tx.getDescription(),
                tx.getCreatedAt(),
				tx.getPaymentKey(),
				pgTid
        );
    }
}
