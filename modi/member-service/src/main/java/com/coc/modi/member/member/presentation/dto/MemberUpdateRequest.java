package com.coc.modi.member.member.presentation.dto;

import com.coc.modi.member.member.application.dto.UpdateMemberCommand;

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
