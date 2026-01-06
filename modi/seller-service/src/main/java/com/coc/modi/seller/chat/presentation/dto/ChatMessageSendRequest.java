package com.coc.modi.seller.chat.presentation.dto;

import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.chat.application.dto.ChatMessageSendCommand;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageSendRequest(
		@NotBlank
		String content
) {
	public ChatMessageSendCommand toCommand(Long roomId, CustomMember sender) {
		return new ChatMessageSendCommand(
				roomId,
				sender.memberId(),
				sender.role(),
				content
		);
	}
}
