package com.coc.modi.member.member.application.dto;

import com.coc.modi.member.member.domain.Member;

public record InternalAdminMemberCreateResponse(
		Long memberId,
		String email,
		String role
) {
	public static InternalAdminMemberCreateResponse from(Member member) {

		return new InternalAdminMemberCreateResponse(
				member.getId(),
				member.getEmail(),
				member.getRole().name()
		);
	}
}
