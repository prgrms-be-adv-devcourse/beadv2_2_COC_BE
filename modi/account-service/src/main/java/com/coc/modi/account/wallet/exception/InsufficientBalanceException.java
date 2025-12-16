package com.coc.modi.account.wallet.exception;

import com.coc.modi.common.ErrorCode;

public class InsufficientBalanceException extends AccountException {
	
	public InsufficientBalanceException(String message) {
		
		super(ErrorCode.ACCOUNT_BALANCE_INSUFFICIENT, message);
	}
}
