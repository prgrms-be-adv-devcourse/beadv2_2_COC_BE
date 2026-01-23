package com.coc.modi.delivery.delivery.exception;

import com.coc.modi.common.ErrorCode;

public class DeliveryConflictException extends DeliveryException {
	
	public DeliveryConflictException(String message) {
		
		super(ErrorCode.DELIVERY_CONFLICT, message);
	}
}
