package com.coc.modi.seller.chat.application.dto;

import java.util.List;

public record ChatMessageSliceResponse(
		List<ChatMessageResponse> messages,
		Long nextCursorId,
		boolean hasNext
) {
}
