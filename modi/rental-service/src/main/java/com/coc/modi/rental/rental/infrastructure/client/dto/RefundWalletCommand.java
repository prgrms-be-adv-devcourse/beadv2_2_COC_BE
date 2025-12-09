package com.coc.modi.rental.rental.infrastructure.client.dto;

import java.math.BigDecimal;

public record RefundWalletCommand(
		Long memberId,
		Long rentalId,
		Long rentalItemId,
		BigDecimal amount
) {
}
