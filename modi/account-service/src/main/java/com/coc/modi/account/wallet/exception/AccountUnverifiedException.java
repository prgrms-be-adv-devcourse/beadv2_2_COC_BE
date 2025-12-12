package com.coc.modi.account.wallet.exception;

import com.coc.modi.common.ErrorCode;

public class AccountUnverifiedException extends AccountException {
	
	public AccountUnverifiedException() {
		
		super(ErrorCode.ACCOUNT_UNVERIFIED);
	}
	
	public AccountUnverifiedException(String message) {
		
		super(ErrorCode.ACCOUNT_UNVERIFIED, message);
	}
}
