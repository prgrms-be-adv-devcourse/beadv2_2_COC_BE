package com.coc.modi.account.wallet.presentation.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RentalPaymentRequest(
		@NotNull(message = "memberId는 필수입니다.")
		Long memberId,
		
		@NotNull(message = "rentalId는 필수입니다.")
		Long rentalId,
		
		@NotNull(message = "amount는 필수입니다.")
		@Positive(message = "amount는 0보다 커야 합니다.")
		BigDecimal amount,
		
		String requestId
) {
}
