package com.coc.modi.account.exception;

import com.coc.modi.common.ErrorCode;

public class AccountLockedException extends AccountException {
	
	public AccountLockedException() {
		
		super(ErrorCode.ACCOUNT_LOCKED);
	}
	
	public AccountLockedException(String message) {
		
		super(ErrorCode.ACCOUNT_LOCKED, message);
	}
}
