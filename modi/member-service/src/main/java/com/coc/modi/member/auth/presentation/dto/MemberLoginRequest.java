package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.MemberLoginCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest(
		@NotBlank
		@Email
		String email,
		
		@NotBlank
		String password
) {
	public MemberLoginCommand toCommand() {
		
		return new MemberLoginCommand(
				email,
				password
		);
	}
}
