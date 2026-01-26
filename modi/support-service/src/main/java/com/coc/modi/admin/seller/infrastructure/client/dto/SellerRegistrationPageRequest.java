package com.coc.modi.admin.seller.infrastructure.client.dto;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.coc.modi.admin.seller.domain.SellerRegistrationStatus;

public record SellerRegistrationPageRequest(
		SellerRegistrationStatus status,
		Integer page,
		Integer size,
		List<String> sort
) {

	public static SellerRegistrationPageRequest from(
			SellerRegistrationStatus status,
			Pageable pageable
	) {

		int page = pageable != null ? pageable.getPageNumber() : 0;
		int size = pageable != null ? pageable.getPageSize() : 20;
		List<String> sort = null;
		if (pageable != null && pageable.getSort() != null) {
			List<String> sorts = pageable.getSort().stream()
					.map(order -> order.getProperty() + "," + order.getDirection().name())
					.toList();
			if (!sorts.isEmpty()) {
				sort = sorts;
			}
		}

		return new SellerRegistrationPageRequest(status, page, size, sort);
	}
}
