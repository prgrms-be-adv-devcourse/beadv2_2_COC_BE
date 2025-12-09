package com.coc.modi.member.presentation.dto;

import com.coc.modi.member.application.dto.UpdateMemberCommand;

public record MemberUpdateRequest(
		String name,
		String phone
) {
	public UpdateMemberCommand toCommand() {
		
		return new UpdateMemberCommand(
				name,
				phone
		);
	}
}
