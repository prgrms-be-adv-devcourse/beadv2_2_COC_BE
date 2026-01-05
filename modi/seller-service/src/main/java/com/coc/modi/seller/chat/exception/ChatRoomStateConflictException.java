package com.coc.modi.seller.chat.exception;

import com.coc.modi.common.ErrorCode;

public class ChatRoomStateConflictException extends ChatException {

	public ChatRoomStateConflictException(String detailMessage) {
		super(ErrorCode.CHAT_CONFLICT, detailMessage);
	}
}
