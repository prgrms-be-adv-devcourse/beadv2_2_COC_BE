package com.coc.modi.member.admin.exception;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.member.member.exception.MemberException;

public class AdminException extends MemberException {

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
