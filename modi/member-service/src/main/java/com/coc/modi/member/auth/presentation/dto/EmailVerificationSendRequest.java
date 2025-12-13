package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.SendEmailVerificationCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationSendRequest(
		@NotBlank
		@Email
		String email
) {
	public SendEmailVerificationCommand toCommand() {
		
		return new SendEmailVerificationCommand(email);
	}
}
