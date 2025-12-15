package com.coc.modi.member.member.application.dto;

public record UpdateMemberCommand(
		Long memberId,
		String name,
		String phone
) {
}
