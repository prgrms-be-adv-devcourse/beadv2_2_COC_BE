package com.coc.modi.admin.notice.application.dto;

import com.coc.modi.admin.notice.domain.Notice;

import java.time.LocalDateTime;

public record NoticeSummaryResponse(
		Long id,
		String title,
		boolean pinned,
		long viewCount,
		LocalDateTime createdAt
) {
	public static NoticeSummaryResponse from(Notice notice) {

		return new NoticeSummaryResponse(
				notice.getId(),
				notice.getTitle(),
				notice.isPinned(),
				notice.getViewCount(),
				notice.getCreatedAt()
		);
	}
}
