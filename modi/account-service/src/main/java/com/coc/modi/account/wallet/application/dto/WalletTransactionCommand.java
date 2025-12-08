package com.coc.modi.account.wallet.application.dto;

import com.coc.modi.account.wallet.domain.WalletTransactionType;

import java.math.BigDecimal;

public record WalletTransactionCommand(
        Long memberId,
        WalletTransactionType txType,
        BigDecimal amount,
        Long relatedPgDepositId,
        Long relatedRentalId,
        Long relatedSettlementId,
        String description
) {
    public static WalletTransactionCommand forRentalPayment(
            Long memberId,
            Long rentalId,
            BigDecimal amount
    ) {

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.RENTAL_PAYMENT,
                amount,
                null,
                rentalId,
                null,
                "렌탈결제"
        );
    }
}
