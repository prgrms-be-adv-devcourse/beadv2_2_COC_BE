package com.coc.modi.member.auth.presentation.dto;

public record EmailVerificationSendResponse(
		String result
) {
	public static EmailVerificationSendResponse success() {
		
		return new EmailVerificationSendResponse("OK");
	}
}
