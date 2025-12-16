package com.coc.modi.member.auth.application.dto;

public record EmailVerificationConfirmResponse(
		boolean verified
) {
	public static EmailVerificationConfirmResponse success() {
		
		return new EmailVerificationConfirmResponse(true);
	}
}
