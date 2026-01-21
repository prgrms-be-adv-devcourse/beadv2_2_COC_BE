package com.coc.modi.ai.exception;

import com.coc.modi.common.ErrorCode;

public class AiInputInvalidException extends AiException {

	public AiInputInvalidException(String detailMessage) {
		super(ErrorCode.CHAT_INVALID_INPUT, detailMessage);
	}
}
