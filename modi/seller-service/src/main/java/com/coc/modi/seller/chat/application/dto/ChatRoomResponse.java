package com.coc.modi.seller.chat.application.dto;

import com.coc.modi.seller.chat.domain.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomResponse(
		Long roomId,
		String roomKey,
		Long sellerId,
		Long memberId,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static ChatRoomResponse from(ChatRoom room, Long sellerId, Long memberId) {
		return new ChatRoomResponse(
				room.getId(),
				room.getRoomKey(),
				sellerId,
				memberId,
				room.getCreatedAt(),
				room.getUpdatedAt()
		);
	}
}
