package com.coc.modi.member.admin.application.dto;

import com.coc.modi.member.member.domain.Member;

public record AdminMemberCreateResponse(
		Long memberId,
		String email,
		String role
) {
	public static AdminMemberCreateResponse from(Member member) {

		return new AdminMemberCreateResponse(
				member.getId(),
				member.getEmail(),
				member.getRole().name()
		);
	}
}
