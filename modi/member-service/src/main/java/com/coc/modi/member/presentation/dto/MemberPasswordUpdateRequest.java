package com.coc.modi.member.presentation.dto;

import com.coc.modi.member.application.dto.UpdateMemberPasswordCommand;

public record MemberPasswordUpdateRequest(
		String name,
		String password
) {
	public UpdateMemberPasswordCommand toCommand() {
		
		return new UpdateMemberPasswordCommand(
				name,
				password
		);
	}
}
