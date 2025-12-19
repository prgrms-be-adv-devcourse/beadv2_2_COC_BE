package com.coc.modi.seller.seller.infrastructure.client.rental.dto;

import java.util.List;


public record RentalListResponse(
        List<RentalItemInfo> rentalItemInfoList,
        Integer totalCount,
        Integer totalPages
) {
    public List<RentalItemInfo> content() {
        return rentalItemInfoList;
    }
}
