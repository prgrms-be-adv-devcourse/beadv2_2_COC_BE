package com.coc.modi.account.wallet.exception;

import com.coc.modi.common.ErrorCode;

public class AccountTransactionNotFoundException extends AccountException {
	
	public AccountTransactionNotFoundException(String transactionKey) {
		
		super(ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND, "거래 정보를 찾을 수 없습니다. key=" + transactionKey);
	}
}
