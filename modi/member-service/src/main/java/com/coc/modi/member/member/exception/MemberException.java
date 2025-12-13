package com.coc.modi.member.member.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class MemberException extends BaseException {
	
	public MemberException(ErrorCode errorCode) {
		
		super(errorCode);
	}
	
	public MemberException(ErrorCode errorCode, String detailMessage) {
		
		super(errorCode, detailMessage);
	}
}
