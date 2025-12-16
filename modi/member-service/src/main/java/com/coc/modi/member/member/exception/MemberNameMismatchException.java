package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class MemberNameMismatchException extends MemberException {
	
	public MemberNameMismatchException(String message) {
		
		super(ErrorCode.NAME_MISMATCH, message);
	}
}
