package com.coc.modi.account.wallet.application.dto;

import com.coc.modi.account.wallet.domain.MemberWallet;

import java.math.BigDecimal;

public record RentalPaymentResponse(
        Long walletId,
        Long memberId,
        BigDecimal balance
) {

    public static RentalPaymentResponse from(MemberWallet wallet) {

        return new RentalPaymentResponse(
                wallet.getId(),
                wallet.getMemberId(),
                wallet.getBalance()
        );
    }
}
