package com.coc.modi.delivery.delivery.application.dto;

import com.coc.modi.delivery.delivery.domain.Delivery;
import com.coc.modi.delivery.delivery.domain.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliveryDetailResponse(
		Long deliveryId,
		Long rentalItemId,
		String carrierCode,
		String trackingNumber,
		DeliveryStatus status,
		String statusRaw,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	
	public static DeliveryDetailResponse from(Delivery delivery) {
		
		return new DeliveryDetailResponse(
				delivery.getId(),
				delivery.getRentalItemId(),
				delivery.getCarrierCode(),
				delivery.getTrackingNumber(),
				delivery.getStatus(),
				delivery.getStatusRaw(),
				delivery.getCreatedAt(),
				delivery.getUpdatedAt());
	}
}
