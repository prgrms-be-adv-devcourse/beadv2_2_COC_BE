package com.coc.modi.member.member.application.dto;

import com.coc.modi.member.member.domain.Member;

public record MemberEmailResponse(
		Long memberId,
		String email
) {
	public static MemberEmailResponse from(Member member) {
		
		return new MemberEmailResponse(
				member.getId(),
				member.getEmail()
		);
	}
}
