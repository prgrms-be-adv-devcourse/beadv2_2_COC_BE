package com.coc.modi.account.exception;

import com.coc.modi.common.ErrorCode;

public class InsufficientBalanceException extends AccountException {
	
	public InsufficientBalanceException() {
		
		super(ErrorCode.ACCOUNT_BALANCE_INSUFFICIENT);
	}
	
	public InsufficientBalanceException(String message) {
		
		super(ErrorCode.ACCOUNT_BALANCE_INSUFFICIENT, message);
	}
}
