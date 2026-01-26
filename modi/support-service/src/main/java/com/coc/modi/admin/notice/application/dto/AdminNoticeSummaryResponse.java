package com.coc.modi.admin.notice.application.dto;

import com.coc.modi.admin.notice.domain.Notice;
import com.coc.modi.admin.notice.domain.NoticeStatus;

import java.time.LocalDateTime;

public record AdminNoticeSummaryResponse(
		Long id,
		String title,
		NoticeStatus status,
		boolean pinned,
		long viewCount,
		LocalDateTime displayStartAt,
		LocalDateTime displayEndAt,
		LocalDateTime createdAt,
		Long createdBy
) {
	public static AdminNoticeSummaryResponse from(Notice notice) {

		return new AdminNoticeSummaryResponse(
				notice.getId(),
				notice.getTitle(),
				notice.getStatus(),
				notice.isPinned(),
				notice.getViewCount(),
				notice.getDisplayStartAt(),
				notice.getDisplayEndAt(),
				notice.getCreatedAt(),
				notice.getCreatedBy()
		);
	}
}
