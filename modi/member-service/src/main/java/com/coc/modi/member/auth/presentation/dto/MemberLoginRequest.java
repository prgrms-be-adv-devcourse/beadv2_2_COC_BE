package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.MemberLoginCommand;

public record MemberLoginRequest(
		String email,
		String password
) {
	public MemberLoginCommand toCommand() {
		
		return new MemberLoginCommand(
				email,
				password
		);
	}
}
