package com.coc.modi.admin.blacklist.exception;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.admin.exception.AdminException;

public class BlacklistException extends AdminException {

	public BlacklistException(ErrorCode errorCode) {
		super(errorCode);
	}

	public BlacklistException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public BlacklistException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
