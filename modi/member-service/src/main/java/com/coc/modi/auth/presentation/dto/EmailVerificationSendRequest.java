package com.coc.modi.auth.presentation.dto;

import com.coc.modi.auth.application.dto.SendEmailVerificationCommand;

public record EmailVerificationSendRequest(
		String email
) {
	public SendEmailVerificationCommand toCommand() {
		
		return new SendEmailVerificationCommand(email);
	}
}
