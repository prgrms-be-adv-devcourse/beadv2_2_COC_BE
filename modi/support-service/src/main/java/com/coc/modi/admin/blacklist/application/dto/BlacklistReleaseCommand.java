package com.coc.modi.admin.blacklist.application.dto;

public record BlacklistReleaseCommand(
		Long memberId,
		String memo,
		Long releasedBy
) {
}
