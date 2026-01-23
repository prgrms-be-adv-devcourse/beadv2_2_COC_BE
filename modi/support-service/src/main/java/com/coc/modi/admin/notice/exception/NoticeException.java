package com.coc.modi.admin.notice.exception;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.admin.exception.AdminException;

public class NoticeException extends AdminException {

	public NoticeException(ErrorCode errorCode) {

		super(errorCode);
	}

	public NoticeException(ErrorCode errorCode, String message) {

		super(errorCode, message);
	}

	public NoticeException(ErrorCode errorCode, String message, Throwable cause) {

		super(errorCode, message, cause);
	}
}
