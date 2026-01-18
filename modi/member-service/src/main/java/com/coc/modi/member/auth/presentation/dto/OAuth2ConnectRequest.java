package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.OAuth2ConnectCommand;

import jakarta.validation.constraints.NotBlank;

public record OAuth2ConnectRequest(
		@NotBlank
		String signupToken
) {

	public OAuth2ConnectCommand toCommand() {

		return new OAuth2ConnectCommand(signupToken);
	}
}
