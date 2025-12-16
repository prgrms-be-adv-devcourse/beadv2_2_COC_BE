package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class MemberEmailMismatchException extends MemberException {
	
	public MemberEmailMismatchException(String message) {
		
		super(ErrorCode.EMAIL_MISMATCH, message);
	}
}
