package com.coc.modi.review.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class ReviewException extends BaseException {

	public ReviewException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ReviewException(ErrorCode errorCode, String message) {
		
		super(errorCode, message);
	}
}
