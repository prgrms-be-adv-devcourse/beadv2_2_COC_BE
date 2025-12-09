package com.coc.modi.member.application.dto;

public record UpdateMemberCommand(
		String name,
		String phone
) {
}
