package com.coc.modi.member.auth.presentation.dto;

public record EmailVerificationConfirmResponse(
		boolean verified
) {
	public static EmailVerificationConfirmResponse success() {
		
		return new EmailVerificationConfirmResponse(true);
	}
}
