package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.PasswordResetSendCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
		@NotBlank
		@Email
		String email
) {
	public PasswordResetSendCommand toCommand() {
		
		return new PasswordResetSendCommand(email);
	}
}
