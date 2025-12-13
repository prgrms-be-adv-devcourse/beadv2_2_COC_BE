package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class WalletCreationFailedException extends MemberException {
	
	public WalletCreationFailedException() {
		
		super(ErrorCode.ACCOUNT_SERVICE_UNAVAILABLE);
	}
}
