package com.coc.modi.member.auth.application.dto;

public record PasswordResetConfirmResponse(
		String resetToken
) {
	public static PasswordResetConfirmResponse of(String resetToken) {

		return new PasswordResetConfirmResponse(resetToken);
	}
}
