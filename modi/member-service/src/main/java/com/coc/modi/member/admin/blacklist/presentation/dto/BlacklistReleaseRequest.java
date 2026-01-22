package com.coc.modi.member.admin.blacklist.presentation.dto;

import com.coc.modi.member.admin.blacklist.application.dto.BlacklistReleaseCommand;

import jakarta.validation.constraints.Size;

public record BlacklistReleaseRequest(
		@Size(max = 1000) String memo
) {

	public BlacklistReleaseCommand toCommand(Long memberId, Long releasedBy) {

		return new BlacklistReleaseCommand(memberId, memo, releasedBy);
	}
}
