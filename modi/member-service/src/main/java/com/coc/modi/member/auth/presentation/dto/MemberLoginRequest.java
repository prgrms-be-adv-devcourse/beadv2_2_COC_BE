package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.MemberLoginCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberLoginRequest(
		@NotBlank
		@Email
		String email,
		
		@NotBlank
		@Pattern(
				regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
				message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 합니다."
		)
		String password
) {
	public MemberLoginCommand toCommand() {
		
		return new MemberLoginCommand(
				email,
				password
		);
	}
}
