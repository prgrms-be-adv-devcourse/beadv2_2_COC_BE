package com.coc.modi.admin.infrastructure.client.member.dto;

public record AdminMemberCreateInternalRequest(
		String email,
		String password,
		String name,
		String phone,
		Long createdBy
) {
}
