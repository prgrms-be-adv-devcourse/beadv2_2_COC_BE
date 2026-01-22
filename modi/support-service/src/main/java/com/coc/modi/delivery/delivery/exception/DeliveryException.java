package com.coc.modi.delivery.delivery.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class DeliveryException extends BaseException {
	
	public DeliveryException(ErrorCode errorCode) {
		
		super(errorCode);
	}
	
	public DeliveryException(ErrorCode errorCode, String message) {
		
		super(errorCode, message);
	}
}
