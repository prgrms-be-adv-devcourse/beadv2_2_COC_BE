package com.coc.modi.account.wallet.application.dto;

import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.wallet.domain.WalletTransactionType;

import java.math.BigDecimal;

public record WalletTransactionCommand(
        Long memberId,
        WalletTransactionType txType,
        BigDecimal amount,
		PgDeposit pgDeposit,
        Long relatedRentalId,
        Long relatedRentalItemId,
        Long relatedSettlementId,
        String description,
		String paymentKey,
		String requestId
) {

    public static WalletTransactionCommand forDepositCharge(
            Long memberId,
            PgDeposit pgDeposit,
            BigDecimal amount,
			String paymentKey
    ) {

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.DEPOSIT_CHARGE,
                amount,
				pgDeposit,
				null,
                null,
                null,
                "예치금 충전",
				paymentKey,
				null
        );
    }

    public static WalletTransactionCommand forRentalPayment(
            Long memberId,
            Long rentalId,
            BigDecimal amount,
			String requestId
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
				null,
				requestId
        );
    }

    public static WalletTransactionCommand forRentalRefund(
            Long memberId,
            Long rentalId,
            Long rentalItemId,
            BigDecimal amount,
            String description,
			String requestId
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
				null,
				requestId
        );
    }

    public static WalletTransactionCommand forDepositCancel(
            Long memberId,
            PgDeposit pgDeposit,
            BigDecimal amount,
			String paymentKey
    ){

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.DEPOSIT_CANCEL,
                amount,
				pgDeposit,
				null,
                null,
                null,
                "예치금 충전 취소",
				paymentKey,
				null
        );
    }

    public static WalletTransactionCommand forSettlementPayout(
            Long memberId,
            Long settlementId,
            BigDecimal amount
    ) {

		return new WalletTransactionCommand(
				memberId,
				WalletTransactionType.SETTLEMENT_PAYOUT,
				amount,
				null,
				null,
				null,
				settlementId,
                "정산금 지급",
                null,
				null
        );
    }

    public static WalletTransactionCommand forWithdrawalRequest(
            Long memberId,
            BigDecimal amount,
            String description
    ) {

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.WITHDRAWAL_REQUEST,
                amount,
                null,
                null,
                null,
                null,
                description != null ? description : "예치금 출금 요청",
                null,
				null
        );
    }

    public static WalletTransactionCommand forWithdrawalRefund(
            Long memberId,
            BigDecimal amount,
            String description
    ) {

        return new WalletTransactionCommand(
                memberId,
                WalletTransactionType.WITHDRAWAL_REFUND,
                amount,
                null,
                null,
                null,
                null,
                description != null ? description : "예치금 출금 실패 환불",
                null,
				null
        );
    }
}
