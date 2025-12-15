package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class PhoneDuplicatedException extends MemberException {
	
	public PhoneDuplicatedException(String phone) {
		
		super(ErrorCode.PHONE_DUPLICATED, "이미 사용 중인 휴대폰입니다. phone=" + phone);
	}
	
	
}
