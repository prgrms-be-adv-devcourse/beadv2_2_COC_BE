package com.coc.modi.seller.chat.exception;

import com.coc.modi.common.ErrorCode;

public class ChatInputInvalidException extends ChatException {

	public ChatInputInvalidException(String detailMessage) {
		super(ErrorCode.CHAT_INVALID_INPUT, detailMessage);
	}
}
