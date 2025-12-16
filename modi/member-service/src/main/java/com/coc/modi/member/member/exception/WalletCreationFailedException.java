package com.coc.modi.member.member.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class WalletCreationFailedException extends BaseException {
	
	public WalletCreationFailedException() {
		super(ErrorCode.ACCOUNT_SERVICE_UNAVAILABLE);
	}
}
