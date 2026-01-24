package com.coc.modi.admin.notice.presentation.dto;

import com.coc.modi.admin.notice.application.dto.NoticeUpdateCommand;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record NoticeUpdateRequest(
		@Size(max = 200, message = "제목은 200자 이내여야 합니다.")
		String title,
		String content,
		Boolean pinned,
		LocalDateTime displayStartAt,
		LocalDateTime displayEndAt
) {
	public NoticeUpdateCommand toCommand(Long noticeId) {

		return new NoticeUpdateCommand(
				noticeId,
				title,
				content,
				pinned,
				displayStartAt,
				displayEndAt
		);
	}
}
