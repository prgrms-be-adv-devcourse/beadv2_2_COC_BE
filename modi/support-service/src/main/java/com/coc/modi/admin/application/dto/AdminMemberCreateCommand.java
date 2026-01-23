package com.coc.modi.admin.application.dto;

public record AdminMemberCreateCommand(
		String email,
		String password,
		String name,
		String phone
) {
}
