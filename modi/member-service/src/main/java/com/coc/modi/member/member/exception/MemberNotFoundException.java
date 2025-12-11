package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class MemberNotFoundException extends MemberException {
	
	public MemberNotFoundException(Long memberId) {
		
		super(ErrorCode.MEMBER_NOT_FOUND, "회원 정보를 찾을 수 없습니다. memberId=" + memberId);
	}
	
	public MemberNotFoundException(String identifier) {
		
		super(ErrorCode.MEMBER_NOT_FOUND, "회원 정보를 찾을 수 없습니다. identifier=" + identifier);
	}
}
