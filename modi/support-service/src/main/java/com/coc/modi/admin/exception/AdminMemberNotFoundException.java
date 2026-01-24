package com.coc.modi.admin.exception;

import com.coc.modi.common.ErrorCode;

public class AdminMemberNotFoundException extends AdminException {

	public AdminMemberNotFoundException(Long memberId) {
		super(ErrorCode.MEMBER_NOT_FOUND, "회원 정보를 찾을 수 없습니다. memberId=" + memberId);
	}

	public AdminMemberNotFoundException(String identifier) {
		super(ErrorCode.MEMBER_NOT_FOUND, "회원 정보를 찾을 수 없습니다. identifier=" + identifier);
	}
}
