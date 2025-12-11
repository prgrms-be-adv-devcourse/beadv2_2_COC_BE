package com.coc.modi.member.member.application.dto;

import com.coc.modi.member.member.domain.Member;

public record MemberProfileResponse(
		Long id,
		String name,
		String phone
) {
	public static MemberProfileResponse from(Member member) {
		
		return new MemberProfileResponse(
				member.getId(),
				member.getName(),
				member.getPhone()
		);
	}
}
