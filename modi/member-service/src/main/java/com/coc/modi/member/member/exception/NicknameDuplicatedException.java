package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class NicknameDuplicatedException extends MemberException {
	
	public NicknameDuplicatedException(String nickname) {
		
		super(ErrorCode.NICKNAME_DUPLICATED, "이미 사용 중인 닉네임입니다. nickname=" + nickname);
	}
}
