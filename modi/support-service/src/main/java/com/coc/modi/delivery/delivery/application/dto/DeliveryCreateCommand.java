package com.coc.modi.delivery.delivery.application.dto;

import com.coc.modi.delivery.delivery.presentation.dto.DeliveryCreateRequest;

public record DeliveryCreateCommand(
		Long rentalItemId,
		String carrierCode,
		String trackingNumber
) {
	
	public static DeliveryCreateCommand toCommand(DeliveryCreateRequest request) {
		
		return new DeliveryCreateCommand(
				request.rentalItemId(),
				request.carrierCode(),
				request.trackingNumber());
	}
}
