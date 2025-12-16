package com.coc.modi.member.member.presentation.dto;

import com.coc.modi.member.member.application.dto.UpdateMemberPasswordCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberPasswordUpdateRequest(
		@NotBlank
		@Size(max = 20)
		String name,
		
		@NotBlank
		@Pattern(
				regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$",
				message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다"
		)
		String password,
		
		@NotBlank
		@Email
		String email,
		
		@NotBlank
		@Pattern(
				regexp = "^\\d{6}$",
				message = "인증코드는 6자리 숫자입니다"
		)
		String verificationCode
) {
	public UpdateMemberPasswordCommand toCommand(Long memberId) {
		
		return new UpdateMemberPasswordCommand(
				memberId,
				name,
				password,
				email,
				verificationCode
		);
	}
}
