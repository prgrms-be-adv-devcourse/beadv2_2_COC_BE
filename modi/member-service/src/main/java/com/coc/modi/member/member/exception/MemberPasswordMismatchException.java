package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class MemberPasswordMismatchException extends MemberException {
	
	public MemberPasswordMismatchException(String message) {
		
		super(ErrorCode.PASSWORD_MISMATCH, message);
	}
}
