package com.coc.modi.rental.rental.infrastructure.client.dto;

import java.time.LocalDate;
import java.util.List;

public record UnavailableProductsRequest(
		LocalDate startDate,
		LocalDate endDate,
		List<Long> productIds
) {
	
	public void vaildate() {
		
		if (startDate == null || endDate == null) {
			
			throw new IllegalArgumentException("시작 날짜와 종료 날짜가 필요합니다.");
		}
		
		if (endDate.isBefore(startDate)) {
			
			throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 나중일 수 없습니다.");
		}
		
		if (productIds == null || productIds.isEmpty()) {
			
			throw new IllegalArgumentException("요청 상품 목록이 없거나 비어 있습니다.");
		}
	}
}
