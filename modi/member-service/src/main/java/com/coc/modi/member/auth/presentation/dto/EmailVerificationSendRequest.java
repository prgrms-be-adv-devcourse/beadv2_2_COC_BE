package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.SendEmailVerificationCommand;

public record EmailVerificationSendRequest(
		String email
) {
	public SendEmailVerificationCommand toCommand() {
		
		return new SendEmailVerificationCommand(email);
	}
}
