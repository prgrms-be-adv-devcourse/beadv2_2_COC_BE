package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.ConfirmEmailVerificationCommand;

public record EmailVerificationConfirmRequest(
		String email,
		String code
) {
	public ConfirmEmailVerificationCommand toCommand() {
		
		return new ConfirmEmailVerificationCommand(email, code);
	}
}
