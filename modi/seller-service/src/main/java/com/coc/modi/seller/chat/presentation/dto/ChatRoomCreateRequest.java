package com.coc.modi.seller.chat.presentation.dto;

import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.chat.application.dto.ChatRoomCreateCommand;

import jakarta.validation.constraints.Positive;

public record ChatRoomCreateRequest(
		@Positive
		Long sellerId,
		@Positive
		Long memberId
) {
	public ChatRoomCreateCommand toCommand(CustomMember requester) {
		return new ChatRoomCreateCommand(
				requester.memberId(),
				requester.role(),
				sellerId,
				memberId
		);
	}
}
