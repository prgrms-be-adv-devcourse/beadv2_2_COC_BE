package com.coc.modi.admin.notice.application.dto;

import java.time.LocalDateTime;

public record NoticeUpdateCommand(
		Long noticeId,
		String title,
		String content,
		Boolean pinned,
		LocalDateTime displayStartAt,
		LocalDateTime displayEndAt
) {
}
