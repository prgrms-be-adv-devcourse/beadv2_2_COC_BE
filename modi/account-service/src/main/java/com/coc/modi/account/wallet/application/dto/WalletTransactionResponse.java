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
        Long relatedPgDepositId,
        Long relatedSettlementId,
        String description,
        LocalDateTime createdAt
) {

    public static WalletTransactionResponse from(WalletTransaction tx) {

        return new WalletTransactionResponse(
                tx.getTxType(),
                tx.getAmount(),
                tx.getBalanceAfter(),
                tx.getRelatedRentalId(),
                tx.getRelatedPgDepositId(),
                tx.getRelatedSettlementId(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}
