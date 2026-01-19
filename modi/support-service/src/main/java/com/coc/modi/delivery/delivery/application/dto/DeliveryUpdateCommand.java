package com.coc.modi.delivery.delivery.application.dto;

import com.coc.modi.delivery.delivery.presentation.dto.DeliveryUpdateRequest;

public record DeliveryUpdateCommand(
		String carrierCode,
		String trackingNumber
) {
	
	public static DeliveryUpdateCommand toCommand(DeliveryUpdateRequest request) {
		
		return new DeliveryUpdateCommand(
				request.carrierCode(),
				request.trackingNumber());
	}
}
