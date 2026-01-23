package com.coc.modi.delivery.delivery.exception;

import com.coc.modi.common.ErrorCode;

public class DeliveryForbiddenException extends DeliveryException {
	
	public DeliveryForbiddenException(String message) {
		
		super(ErrorCode.FORBIDDEN, message);
	}
}
