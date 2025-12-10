package com.coc.modi.auth.application.dto;

public record ConfirmEmailVerificationCommand(
		String email,
		String code
) {
}
