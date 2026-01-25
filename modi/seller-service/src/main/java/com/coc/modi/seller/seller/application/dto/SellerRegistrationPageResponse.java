package com.coc.modi.seller.seller.application.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record SellerRegistrationPageResponse(
		List<SellerRegistrationResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean last
) {

	public static SellerRegistrationPageResponse from(Page<SellerRegistrationResponse> registrations) {

		return new SellerRegistrationPageResponse(
				registrations.getContent(),
				registrations.getNumber(),
				registrations.getSize(),
				registrations.getTotalElements(),
				registrations.getTotalPages(),
				registrations.isLast()
		);
	}
}
