package com.coc.modi.ai.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class AiException extends BaseException {

	public AiException(ErrorCode errorCode) {
		super(errorCode);
	}

	public AiException(ErrorCode errorCode, String detailMessage) {
		super(errorCode, detailMessage);
	}

	public AiException(ErrorCode errorCode, String detailMessage, Throwable cause) {
		super(errorCode, detailMessage, cause);
	}
}
