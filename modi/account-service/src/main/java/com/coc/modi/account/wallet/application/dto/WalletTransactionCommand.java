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
        String description,
		String paymentKey
) {

    public static WalletTransactionCommand forDepositCharge(
            Long memberId,
            Long pgDepositId,
            BigDecimal amount,
			String paymentKey
    ) {

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.DEPOSIT_CHARGE,
                amount,
                pgDepositId,
                null,
                null,
                null,
                "예치금 충전",
				paymentKey
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
                "렌탈결제",
				null
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
                description,
				null
        );
    }

    public static WalletTransactionCommand forDepositCancel(
            Long memberId,
            Long depositId,
            BigDecimal amount,
			String paymentKey
    ){

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.DEPOSIT_CANCEL,
                amount,
                depositId,
                null,
                null,
                null,
                "예치금 충전 취소",
				paymentKey
        );
    }
}
