package com.coc.modi.account.wallet.application.dto;

import com.coc.modi.account.wallet.presentation.dto.RentalRefundRequest;

import java.math.BigDecimal;

public record RentalRefundCommand(
        Long memberId,
        Long rentalId,
        Long rentalItemId,
        BigDecimal amount
) {

    public static RentalRefundCommand from(RentalRefundRequest request) {

        return new RentalRefundCommand(
                request.memberId(),
                request.rentalId(),
                request.rentalItemId(),
                request.amount()
        );
    }
}
