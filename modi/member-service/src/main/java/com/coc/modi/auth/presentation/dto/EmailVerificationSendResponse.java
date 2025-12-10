package com.coc.modi.auth.presentation.dto;

public record EmailVerificationSendResponse(
		String result
) {
	public static EmailVerificationSendResponse success() {
		
		return new EmailVerificationSendResponse("OK");
	}
}
