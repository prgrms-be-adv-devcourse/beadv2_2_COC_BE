package com.coc.modi.delivery.delivery.exception;

import com.coc.modi.common.ErrorCode;

public class DeliveryNotFoundException extends DeliveryException {
	
	public DeliveryNotFoundException(Long deliveryId) {
		
		super(ErrorCode.DELIVERY_NOT_FOUND, "배송 정보를 찾을 수 없습니다. deliveryId: " + deliveryId);
	}
}
