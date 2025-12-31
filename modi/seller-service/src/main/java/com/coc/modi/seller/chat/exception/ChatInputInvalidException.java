package com.coc.modi.seller.chat.exception;

import com.coc.modi.common.ErrorCode;

public class ChatInputInvalidException extends ChatException {

	public ChatInputInvalidException(String detailMessage) {
		super(ErrorCode.INVALID_INPUT, detailMessage);
	}
}
