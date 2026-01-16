package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class MemberWithdrawnException extends MemberException {
	
	public MemberWithdrawnException(String message) {
		
		super(ErrorCode.MEMBER_WITHDRAWN, message);
	}
}
