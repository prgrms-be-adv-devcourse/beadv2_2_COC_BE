package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class MemberAccessDeniedException extends MemberException{
	
	public MemberAccessDeniedException(String message) {
		
		super(ErrorCode.MEMER_ACCESS_DENIED, message);
	}
}
