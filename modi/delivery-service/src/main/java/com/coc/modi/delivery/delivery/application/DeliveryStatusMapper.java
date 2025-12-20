package com.coc.modi.delivery.delivery.application;

import org.springframework.stereotype.Component;

import com.coc.modi.delivery.delivery.domain.DeliveryStatus;
import com.coc.modi.delivery.delivery.infrastructure.TrackingResult;

@Component
public class DeliveryStatusMapper {
	
	public DeliveryStatus map(TrackingResult result) {
		
		if (result.delivered()) {
			return DeliveryStatus.DELIVERED;
		}
		
		return switch (result.rawStatus()) {
			case "AT_PICKUP" -> DeliveryStatus.PICKED_UP;
			case "IN_TRANSIT" -> DeliveryStatus.IN_TRANSIT;
			case "OUT_FOR_DELIVERY", "AVAILABLE_FOR_PICKU" -> DeliveryStatus.OUT_FOR_DELIVERY;
			case "DELIVERED" -> DeliveryStatus.DELIVERED;
			case "ATTEMPT_FAIL", "EXCEPTION" -> DeliveryStatus.EXCEPTION;
			case "CANCELLED" -> DeliveryStatus.CANCELLED;
			default -> DeliveryStatus.REGISTERED;
		};
	}
}
