package com.coc.modi.account.deposit.presentation.dto;

import com.coc.modi.account.deposit.application.dto.DepositFailCommand;

import jakarta.validation.constraints.NotBlank;

public record DepositFailRequest(
		@NotBlank String orderId,
		@NotBlank String code,
		@NotBlank String message
) {
	public DepositFailCommand toCommand() {
		
		return new DepositFailCommand(orderId, code, message);
	}
}
