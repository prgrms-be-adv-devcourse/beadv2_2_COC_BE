package com.coc.modi.auth.presentation.dto;

public record EmailVerificationConfirmResponse(
		boolean verified
) {
	public static EmailVerificationConfirmResponse success() {
		
		return new EmailVerificationConfirmResponse(true);
	}
}
