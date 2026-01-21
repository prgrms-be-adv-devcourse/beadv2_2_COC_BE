package com.coc.modi.seller.chat.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class ChatException extends BaseException {

	public ChatException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ChatException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public ChatException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
