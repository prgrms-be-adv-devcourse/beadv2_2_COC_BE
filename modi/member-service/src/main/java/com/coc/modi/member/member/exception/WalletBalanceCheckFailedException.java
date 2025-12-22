package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class WalletBalanceCheckFailedException extends MemberException {
	
	public WalletBalanceCheckFailedException() {
		
		super(ErrorCode.ACCOUNT_BALENCE_CHECK);
	}
}
