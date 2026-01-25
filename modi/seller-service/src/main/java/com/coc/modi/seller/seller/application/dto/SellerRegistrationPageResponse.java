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

	public static SellerRegistrationPageResponse from(Page<SellerRegistrationResponse> page) {

		if (page == null) {
			return new SellerRegistrationPageResponse(List.of(), 0, 0, 0L, 0, true);
		}
		return new SellerRegistrationPageResponse(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isLast()
		);
	}
}
