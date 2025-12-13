package com.coc.modi.rental.rental.infrastructure.client.dto;


import java.util.List;

public record RentalItemInfoListResponse(
		
		List<RentalItemInfo> rentalItemInfoList,
		Long totalCount,
		Integer totalPages
) {
}
