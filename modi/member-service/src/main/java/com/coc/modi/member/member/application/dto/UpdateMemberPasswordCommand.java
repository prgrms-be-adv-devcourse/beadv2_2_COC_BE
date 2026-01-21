package com.coc.modi.member.member.application.dto;

public record UpdateMemberPasswordCommand(
		Long memberId,
		String currentPassword,
		String newPassword
) {
}
