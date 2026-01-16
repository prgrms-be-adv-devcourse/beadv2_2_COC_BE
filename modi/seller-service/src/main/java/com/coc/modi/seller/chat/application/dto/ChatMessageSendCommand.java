package com.coc.modi.seller.chat.application.dto;

public record ChatMessageSendCommand(
		Long roomId,
		Long senderId,
		String senderRole,
		String content
) {
}
