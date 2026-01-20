package com.coc.modi.delivery.delivery.exception;

import com.coc.modi.common.ErrorCode;

public class DeliveryTrackingClientException extends DeliveryException {
	
	public DeliveryTrackingClientException(String message) {
		
		super(ErrorCode.DELIVERY_TRACKING_ERROR, message);
	}
}
