package com.coc.modi.admin.exception;

import com.coc.modi.common.ErrorCode;

public class AdminAccessDeniedException extends AdminException {

	public AdminAccessDeniedException(String message) {
		super(ErrorCode.ADMIN_ACCESS_DENIED, message);
	}
}
