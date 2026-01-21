package com.coc.modi.account.wallet.application.dto;

import com.coc.modi.account.wallet.presentation.dto.RentalPaymentRequest;

import java.math.BigDecimal;

public record RentalPaymentCommand(
        Long memberId,
        Long rentalId,
        BigDecimal amount,
		String requestId
) {

    public static RentalPaymentCommand from(RentalPaymentRequest request) {

        return new RentalPaymentCommand(
                request.memberId(),
                request.rentalId(),
                request.amount(),
				request.requestId()
        );
    }
}
