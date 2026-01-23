package com.coc.modi.admin.infrastructure.client.member.dto;

import java.util.List;

public record MemberPageResponse(
		List<MemberSummaryResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean last
) {
}
