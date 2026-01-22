package com.coc.modi.member.admin.blacklist.exception;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.member.admin.exception.AdminException;

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
