package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.OAuth2SignupCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OAuth2SignupRequest(
		@NotBlank
		String signupToken,

		@NotBlank
		@Email
		String email,

		@NotBlank
		@Pattern(
				regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
				message = "올바른 휴대폰 번호 형식이 아닙니다"
		)
		String phone,

		@NotBlank
		String verificationToken
) {

	public OAuth2SignupCommand toCommand() {

		return new OAuth2SignupCommand(signupToken, email, phone, verificationToken);
	}
}
