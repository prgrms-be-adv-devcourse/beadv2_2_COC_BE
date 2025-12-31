package com.coc.modi.seller.chat.exception;

import com.coc.modi.common.ErrorCode;

public class ChatAccessDeniedException extends ChatException {

	public ChatAccessDeniedException(String detailMessage) {
		super(ErrorCode.FORBIDDEN, detailMessage);
	}
}
