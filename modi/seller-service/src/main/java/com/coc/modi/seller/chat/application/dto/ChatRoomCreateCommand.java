package com.coc.modi.seller.chat.application.dto;

public record ChatRoomCreateCommand(
		Long requesterMemberId,
		String requesterRole,
		Long sellerId,
		Long memberId
) {
}
