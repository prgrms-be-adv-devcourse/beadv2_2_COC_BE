package com.coc.modi.seller.chat.exception;

import com.coc.modi.common.ErrorCode;

public class ChatRoomNotFoundException extends ChatException {

	public ChatRoomNotFoundException(String detailMessage) {
		super(ErrorCode.NOT_FOUND, detailMessage);
	}
}
