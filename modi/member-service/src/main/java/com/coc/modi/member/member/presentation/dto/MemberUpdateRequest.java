package com.coc.modi.member.member.presentation.dto;

import com.coc.modi.member.member.application.dto.UpdateMemberCommand;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
		@Size(max = 20)
		String name,
		
		@Pattern(
				regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
				message = "올바른 휴대폰 번호 형식이 아닙니다"
		)
		String phone
) {
	public UpdateMemberCommand toCommand() {
		
		return new UpdateMemberCommand(
				name,
				phone
		);
	}
}
