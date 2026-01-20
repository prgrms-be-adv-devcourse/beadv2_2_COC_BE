package com.coc.modi.delivery.delivery.infrastructure.dto;

import java.util.List;

public record TrackerDeliveryResponse(
		Data data,
		List<ApiError> errors
) {
	public record Data(
			Track track
	) {
	}
	
	public record Track(
			String trackingNumber,
			LastEvent lastEvent
	) {
	}
	
	public record LastEvent(
			String description,
			Status status
	) {
	}
	
	public record Status(
			String code,    // IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED ...
			String name     // 배송중, 배송출발, 배송완료 ...
	) {
	}
	
	public record ApiError(
			String message
	) {
	}
}
