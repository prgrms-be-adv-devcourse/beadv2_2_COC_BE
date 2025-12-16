package com.coc.modi.member.auth.application.dto;

import org.springframework.http.ResponseCookie;

import com.coc.modi.member.member.domain.Member;

public record MemberLoginResponse(
		String accessToken,
		MemberData member,
		ResponseCookie refreshCookie
) {
	
	public record MemberData(
			Long memberId,
			String email,
			String name,
			String role
	) {
		public static MemberData from(Member member) {
			
			return new MemberData(
					member.getId(),
					member.getEmail(),
					member.getName(),
					member.getRole().name()
			);
		}
	}
}
