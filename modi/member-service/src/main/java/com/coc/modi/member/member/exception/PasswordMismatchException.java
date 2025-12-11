package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class PasswordMismatchException extends MemberException {
	
	public PasswordMismatchException() {
		
		super(ErrorCode.PASSWORD_MISMATCH);
	}
	
	public PasswordMismatchException(String message) {
		
		super(ErrorCode.PASSWORD_MISMATCH, message);
	}
}
