package com.coc.modi.seller.settlement.infrastructure.client.dto;

import java.util.List;

public record RentalListResponse(
        List<RentalSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
