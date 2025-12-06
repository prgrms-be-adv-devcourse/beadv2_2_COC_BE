package com.coc.modi.seller.infrastructure.client.rental.dto;

import java.util.List;

public record RentalListResponse(
        List<RentalSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
