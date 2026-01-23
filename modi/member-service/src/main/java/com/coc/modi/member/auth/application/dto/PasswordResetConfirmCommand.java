package com.coc.modi.member.auth.application.dto;

public record PasswordResetConfirmCommand(
		String email,
		String code
) {
}
