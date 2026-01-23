package com.coc.modi.member.member.application.dto;

import java.time.LocalDateTime;

import com.coc.modi.member.member.domain.Member;

public record MemberSummaryResponse(
		Long memberId,
		String email,
		String name,
		String phone,
		String status,
		String role,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static MemberSummaryResponse from(Member member) {

		return new MemberSummaryResponse(
				member.getId(),
				member.getEmail(),
				member.getName(),
				member.getPhone(),
				member.getStatus().name(),
				member.getRole().name(),
				member.getCreatedAt(),
				member.getUpdatedAt()
		);
	}
}
