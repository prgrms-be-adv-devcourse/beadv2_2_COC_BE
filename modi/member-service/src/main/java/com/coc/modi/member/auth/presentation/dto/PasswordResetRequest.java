package com.coc.modi.member.auth.presentation.dto;

import com.coc.modi.member.auth.application.dto.PasswordResetSendCommand;

public record PasswordResetRequest(
		String email
) {
	public PasswordResetSendCommand toCommand() {
		
		return new PasswordResetSendCommand(email);
	}
}
