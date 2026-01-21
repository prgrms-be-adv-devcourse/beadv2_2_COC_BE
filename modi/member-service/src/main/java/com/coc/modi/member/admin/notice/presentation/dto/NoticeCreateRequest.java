package com.coc.modi.member.admin.notice.presentation.dto;

import com.coc.modi.member.admin.notice.application.dto.NoticeCreateCommand;
import com.coc.modi.member.admin.notice.domain.NoticeStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record NoticeCreateRequest(
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 200, message = "제목은 200자 이내여야 합니다.")
		String title,
		@NotBlank(message = "내용은 필수입니다.")
		String content,
		Boolean pinned,
		NoticeStatus status,
		LocalDateTime displayStartAt,
		LocalDateTime displayEndAt
) {
	public NoticeCreateCommand toCommand(Long createdBy) {

		return new NoticeCreateCommand(
				title,
				content,
				status,
				Boolean.TRUE.equals(pinned),
				displayStartAt,
				displayEndAt,
				createdBy
		);
	}
}
