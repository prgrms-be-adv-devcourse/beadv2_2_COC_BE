package com.coc.modi.product.application.dto;

import java.util.List;

public record ProductScrollResponse(
		List<ProductListResponse> products,
		String nextCursor,
		boolean hasNext
) {
}
