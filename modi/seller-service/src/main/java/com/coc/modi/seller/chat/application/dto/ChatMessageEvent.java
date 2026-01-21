package com.coc.modi.seller.chat.application.dto;

import com.coc.modi.seller.chat.domain.ChatMessage;
import com.coc.modi.seller.chat.domain.ChatParticipantRole;

import java.time.LocalDateTime;

public record ChatMessageEvent(
		Long messageId,
		Long roomId,
		Long senderId,
		ChatParticipantRole senderRole,
		String content,
		LocalDateTime sentAt
) {
	public static ChatMessageEvent from(ChatMessage message) {
		return new ChatMessageEvent(
				message.getId(),
				message.getRoom().getId(),
				message.getSenderId(),
				message.getSenderRole(),
				message.getContent(),
				message.getSentAt()
		);
	}
}
