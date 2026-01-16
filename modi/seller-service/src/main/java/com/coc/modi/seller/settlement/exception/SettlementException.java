package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SettlementException extends BaseException {
	
	public SettlementException(ErrorCode errorCode) {
		
		super(errorCode);
	}
	
	public SettlementException(ErrorCode errorCode, String message) {
		
		super(errorCode, message);
	}

	public SettlementException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
