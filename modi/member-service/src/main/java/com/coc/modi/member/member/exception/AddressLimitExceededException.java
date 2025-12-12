package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class AddressLimitExceededException extends MemberException {
	
	public AddressLimitExceededException(int limit) {
		
		super(ErrorCode.ADDRESS_LIMIT_EXCEEDED, "등록 가능한 주소 수를 초과했습니다. limit=" + limit);
	}
	
	public AddressLimitExceededException() {
		
		super(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
	}
}
