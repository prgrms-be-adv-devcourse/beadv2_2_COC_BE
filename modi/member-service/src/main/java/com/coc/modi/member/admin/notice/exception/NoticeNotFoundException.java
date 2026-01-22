package com.coc.modi.member.admin.notice.exception;

import com.coc.modi.common.ErrorCode;

public class NoticeNotFoundException extends NoticeException {

	public NoticeNotFoundException(String message) {

		super(ErrorCode.ADMIN_NOTICE_NOT_FOUND, message);
	}
}
