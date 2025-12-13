package com.coc.modi.member.member.application.dto;

public record UpdateMemberPasswordCommand(
		String name,
		String password,
		String email,
		String verificationCode
) {
}
