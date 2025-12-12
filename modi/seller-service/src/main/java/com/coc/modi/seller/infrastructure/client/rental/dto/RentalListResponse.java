package com.coc.modi.seller.infrastructure.client.rental.dto;

import java.util.List;

/**
 * rental-service 내부 API(/internal/rentals) 응답 포맷에 맞춘 DTO.
 * 필드 이름을 rental-service에 맞추고, 기존 코드 호환을 위해 content() 헬퍼를 제공한다.
 */
public record RentalListResponse(
        List<RentalItemInfo> rentalItemInfoList,
        Integer totalCount,
        Integer totalPages
) {
    public List<RentalItemInfo> content() {
        return rentalItemInfoList;
    }
}
