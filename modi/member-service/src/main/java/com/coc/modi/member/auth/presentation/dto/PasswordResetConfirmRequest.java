package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.PasswordResetConfirmCommand;

public record PasswordResetConfirmRequest(
		String email,
		String code,
		String newPassword
) {
	public PasswordResetConfirmCommand toCommand() {
		
		return new PasswordResetConfirmCommand(email, code, newPassword);
	}
}
