package com.coc.modi.member.application.dto;

public record UpdateMemberPasswordCommand(
		String name,
		String password
) {
}
