package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class AuthCodeInvalidException extends MemberException {
	
	public AuthCodeInvalidException(String message) {
		
		super(ErrorCode.AUTH_CODE_INVALID, message);
	}
}
