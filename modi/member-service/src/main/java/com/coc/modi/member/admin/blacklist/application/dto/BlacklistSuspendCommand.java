package com.coc.modi.member.admin.blacklist.application.dto;

public record BlacklistSuspendCommand(
		Long memberId,
		String reason,
		String memo,
		Long createdBy
) {
}
