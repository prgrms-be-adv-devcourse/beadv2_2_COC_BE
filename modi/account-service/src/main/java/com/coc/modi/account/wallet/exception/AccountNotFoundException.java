package com.coc.modi.account.wallet.exception;

import com.coc.modi.common.ErrorCode;

public class AccountNotFoundException extends AccountException {
	
	public AccountNotFoundException(Long accountId) {
		
		super(ErrorCode.ACCOUNT_NOT_FOUND, "지갑 정보를 찾을 수 없습니다. accountId=" + accountId);
	}
	
}
