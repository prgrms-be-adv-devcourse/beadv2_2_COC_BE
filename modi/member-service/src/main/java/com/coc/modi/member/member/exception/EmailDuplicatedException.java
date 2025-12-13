package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class EmailDuplicatedException extends MemberException {
	
	public EmailDuplicatedException(String email) {
		
		super(ErrorCode.EMAIL_DUPLICATED, "이미 사용 중인 이메일입니다. email=" + email);
	}
}
