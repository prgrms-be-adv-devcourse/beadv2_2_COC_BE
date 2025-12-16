package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.ConfirmEmailVerificationCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerificationConfirmRequest(
		@NotBlank
		@Email
		String email,
		
		@NotBlank
		@Pattern(
				regexp = "^\\d{6}$",
				message = "인증코드는 6자리 숫자입니다"
		)
		String code
) {
	public ConfirmEmailVerificationCommand toCommand() {
		
		return new ConfirmEmailVerificationCommand(email, code);
	}
}
