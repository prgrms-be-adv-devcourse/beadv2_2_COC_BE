package com.coc.modi.member.auth.application.dto;

public record ConfirmEmailVerificationCommand(
		String email,
		String code
) {
}
