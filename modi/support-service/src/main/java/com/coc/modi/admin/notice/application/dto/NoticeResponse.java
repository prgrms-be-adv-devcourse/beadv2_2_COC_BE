package com.coc.modi.admin.notice.application.dto;

import com.coc.modi.admin.notice.domain.Notice;
import com.coc.modi.admin.notice.domain.NoticeStatus;

import java.time.LocalDateTime;

public record NoticeResponse(
		Long id,
		String title,
		String content,
		NoticeStatus status,
		boolean pinned,
		long viewCount,
		LocalDateTime displayStartAt,
		LocalDateTime displayEndAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static NoticeResponse from(Notice notice) {

		return new NoticeResponse(
				notice.getId(),
				notice.getTitle(),
				notice.getContent(),
				notice.getStatus(),
				notice.isPinned(),
				notice.getViewCount(),
				notice.getDisplayStartAt(),
				notice.getDisplayEndAt(),
				notice.getCreatedAt(),
				notice.getUpdatedAt()
		);
	}
}
