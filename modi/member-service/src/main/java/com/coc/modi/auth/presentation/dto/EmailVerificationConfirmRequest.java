package com.coc.modi.auth.presentation.dto;

import com.coc.modi.auth.application.dto.ConfirmEmailVerificationCommand;

public record EmailVerificationConfirmRequest(
		String email,
		String code
) {
	public ConfirmEmailVerificationCommand toCommand() {
		
		return new ConfirmEmailVerificationCommand(email, code);
	}
}
