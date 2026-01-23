package com.coc.modi.admin.application.dto;

public record AdminMemberCreateResponse(
		Long memberId,
		String email,
		String role
) {
}
