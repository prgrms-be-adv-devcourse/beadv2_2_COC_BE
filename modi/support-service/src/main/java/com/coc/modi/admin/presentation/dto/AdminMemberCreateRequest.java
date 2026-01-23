package com.coc.modi.admin.presentation.dto;

import com.coc.modi.admin.application.dto.AdminMemberCreateCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminMemberCreateRequest(
		@NotBlank
		@Email
		String email,

		@NotBlank
		@Pattern(
				regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$",
				message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다"
		)
		String password,

		@NotBlank
		@Size(max = 20)
		String name,

		@NotBlank
		@Pattern(
				regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
				message = "올바른 휴대폰 번호 형식이 아닙니다"
		)
		String phone
) {
	public AdminMemberCreateCommand toCommand() {

		return new AdminMemberCreateCommand(
				email,
				password,
				name,
				phone
		);
	}
}
