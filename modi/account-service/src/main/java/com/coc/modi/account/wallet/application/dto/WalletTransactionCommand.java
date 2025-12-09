package com.coc.modi.account.wallet.application.dto;

import com.coc.modi.account.wallet.domain.WalletTransactionType;

import java.math.BigDecimal;

public record WalletTransactionCommand(
        Long memberId,
        WalletTransactionType txType,
        BigDecimal amount,
        Long relatedPgDepositId,
        Long relatedRentalId,
        Long relatedRentalItemId,
        Long relatedSettlementId,
        String description
) {

    public static WalletTransactionCommand forDepositCharge(
            Long memberId,
            Long pgDepositId,
            BigDecimal amount
    ) {

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.DEPOSIT_CHARGE,
                amount,
                pgDepositId,
                null,
                null,
                null,
                "예치금 충전"
        );
    }

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
                null,
                "렌탈결제"
        );
    }

    public static WalletTransactionCommand forRentalRefund(
            Long memberId,
            Long rentalId,
            Long rentalItemId,
            BigDecimal amount,
            String description
    ) {

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.RENTAL_REFUND,
                amount,
                null,
                rentalId,
                rentalItemId,
                null,
                description
        );
    }

    public static WalletTransactionCommand forDepositCancel(
            Long memberId,
            Long depositId,
            BigDecimal amount
    ){

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.DEPOSIT_CANCEL,
                amount,
                depositId,
                null,
                null,
                null,
                "예치금 충전 취소"
        );
    }
}
