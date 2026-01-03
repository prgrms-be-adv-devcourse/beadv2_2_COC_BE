package com.coc.modi.member.auth.application.dto;

public record EmailVerificationConfirmResponse(
		boolean verified,
		String verificationToken
) {
	public static EmailVerificationConfirmResponse success(String verificationToken) {
		
		return new EmailVerificationConfirmResponse(true, verificationToken);
	}
}
