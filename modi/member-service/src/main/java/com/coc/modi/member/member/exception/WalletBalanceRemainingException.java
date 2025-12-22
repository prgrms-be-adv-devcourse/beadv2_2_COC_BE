package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class WalletBalanceRemainingException extends MemberException {
	
	public WalletBalanceRemainingException() {
		
		super(ErrorCode.ACCOUNT_BALENCE_REMAIN);
	}
}
