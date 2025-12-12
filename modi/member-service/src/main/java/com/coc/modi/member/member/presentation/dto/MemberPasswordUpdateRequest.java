package com.coc.modi.member.member.presentation.dto;

import com.coc.modi.member.member.application.dto.UpdateMemberPasswordCommand;

public record MemberPasswordUpdateRequest(
		String name,
		String password,
		String email
) {
	public UpdateMemberPasswordCommand toCommand() {
		
		return new UpdateMemberPasswordCommand(
				name,
				password,
				email
		);
	}
}
