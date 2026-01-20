package com.coc.modi.member.admin.blacklist.presentation.dto;

import com.coc.modi.member.admin.blacklist.application.dto.BlacklistSuspendCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BlacklistSuspendRequest(
		@NotNull Long memberId,
		@NotBlank @Size(max = 500) String reason,
		@Size(max = 1000) String memo
) {

	public BlacklistSuspendCommand toCommand(Long createdBy) {

		return new BlacklistSuspendCommand(memberId, reason, memo, createdBy);
	}
}
