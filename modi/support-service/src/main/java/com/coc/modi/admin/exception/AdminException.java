package com.coc.modi.admin.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class AdminException extends BaseException {

	public AdminException(ErrorCode errorCode) {
		super(errorCode);
	}

	public AdminException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public AdminException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
