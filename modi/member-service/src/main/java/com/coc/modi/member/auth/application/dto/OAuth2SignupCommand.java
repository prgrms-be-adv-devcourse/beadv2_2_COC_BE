package com.coc.modi.member.auth.application.dto;

public record OAuth2SignupCommand(
		String signupToken,
		String email,
		String phone,
		String verificationToken
) {
}
