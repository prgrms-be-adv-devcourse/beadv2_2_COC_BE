package com.coc.modi.member.auth.application.dto;

public record MemberLoginCommand(
		String email,
		String password
) {
}
