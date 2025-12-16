package com.coc.modi.account.wallet.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class AccountException extends BaseException {
	
	public AccountException(ErrorCode errorCode, String detailMessage) {
		
		super(errorCode, detailMessage);
	}
}
