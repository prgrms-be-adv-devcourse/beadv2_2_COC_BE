package com.coc.modi.member.admin.application.dto;

public record AdminMemberCreateCommand(
		String email,
		String password,
		String name,
		String phone
) {
}
