package com.coc.modi.admin.notice.application.dto;

import java.time.LocalDateTime;

import com.coc.modi.admin.notice.domain.NoticeStatus;

public record NoticeCreateCommand(
		String title,
		String content,
		NoticeStatus status,
		boolean pinned,
		LocalDateTime displayStartAt,
		LocalDateTime displayEndAt,
		Long createdBy
) {
}
