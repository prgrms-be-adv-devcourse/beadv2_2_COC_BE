package com.coc.modi.rental.rental.infrastructure.client.dto;


import java.util.List;

public record RentalItemInfoListResponse(
		
		List<RentalItemInfo> rentalItemInfoList,
		Integer totalCount,
		Integer totalPages
) {
}
