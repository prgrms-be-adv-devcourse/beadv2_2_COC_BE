package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.PasswordResetCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetUpdateRequest(
		@NotBlank
		String resetToken,

		@NotBlank
		@Pattern(
				regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$",
				message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다"
		)
		String newPassword
) {
	public PasswordResetCommand toCommand() {

		return new PasswordResetCommand(resetToken, newPassword);
	}
}
