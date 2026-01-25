package com.coc.modi.admin.seller.infrastructure.client.dto;

import java.util.List;

import com.coc.modi.admin.seller.application.dto.SellerRegistrationResponse;

public record SellerRegistrationPageResponse(
		List<SellerRegistrationResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean last
) {
}
