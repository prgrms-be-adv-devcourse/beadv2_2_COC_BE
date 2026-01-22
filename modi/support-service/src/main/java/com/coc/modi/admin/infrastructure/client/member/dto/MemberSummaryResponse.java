package com.coc.modi.admin.infrastructure.client.member.dto;

import java.time.LocalDateTime;

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
}
