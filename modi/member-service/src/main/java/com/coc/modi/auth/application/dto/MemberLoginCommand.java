package com.coc.modi.auth.application.dto;

public record MemberLoginCommand(
		String email,
		String password
) {
}
