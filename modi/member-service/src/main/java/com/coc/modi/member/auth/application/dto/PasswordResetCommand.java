package com.coc.modi.member.auth.application.dto;

public record PasswordResetCommand(
		String resetToken,
		String newPassword
) {
}
