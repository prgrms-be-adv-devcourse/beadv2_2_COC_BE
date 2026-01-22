package com.coc.modi.member.member.application.dto;

public record InternalAdminMemberCreateCommand(
		String email,
		String password,
		String name,
		String phone,
		Long createdBy
) {
}
