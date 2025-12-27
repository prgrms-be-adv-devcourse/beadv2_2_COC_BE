package com.coc.modi.member.member.application.dto;

public record CreateMemberCommand(
		String email,
		String password,
		String name,
		String phone,
		String verificationToken
) {
}
