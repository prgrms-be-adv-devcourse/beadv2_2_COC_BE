package com.coc.modi.delivery.delivery.application.dto;

import com.coc.modi.delivery.delivery.domain.DeliveryStatus;

public record DeliveryCreateResponse(
		Long deliveryId,
		Long rentalItemId,
		String carrierCode,
		String trackingNumber,
		DeliveryStatus status
) {
}
