package com.coc.modi.member.admin.blacklist.application.dto;

public record BlacklistReleaseCommand(
		Long memberId,
		String memo,
		Long releasedBy
) {
}
