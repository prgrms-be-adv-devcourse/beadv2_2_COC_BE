package com.coc.modi.account.wallet.exception;

import com.coc.modi.common.ErrorCode;

public class AccountAlreadyExistsException extends AccountException {
	
	public AccountAlreadyExistsException(String accountKey) {
		
		super(ErrorCode.ACCOUNT_ALREADY_EXISTS, "계정이 이미 존재합니다. key=" + accountKey);
	}
}
