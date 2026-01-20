package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.PasswordResetConfirmCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmRequest(
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
	public PasswordResetConfirmCommand toCommand() {
		
		return new PasswordResetConfirmCommand(email, code);
	}
}
