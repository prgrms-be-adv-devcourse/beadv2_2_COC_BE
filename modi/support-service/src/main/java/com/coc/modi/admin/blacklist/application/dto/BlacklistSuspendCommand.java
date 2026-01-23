package com.coc.modi.admin.blacklist.application.dto;

public record BlacklistSuspendCommand(
		Long memberId,
		String reason,
		String memo,
		Long createdBy
) {
}
