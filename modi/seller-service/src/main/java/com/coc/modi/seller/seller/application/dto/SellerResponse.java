package com.coc.modi.seller.seller.application.dto;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerStatus;

import java.time.LocalDateTime;

public record SellerResponse(
        Long id,
        Long memberId,
        String storeName,
        String bizRegNo,
        String storePhone,
        SellerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SellerResponse from(Seller seller) {
        return new SellerResponse(
                seller.getId(),
                seller.getMemberId(),
                seller.getStoreName(),
                seller.getBizRegNo(),
                seller.getStorePhone(),
                seller.getStatus(),
                seller.getCreatedAt(),
                seller.getUpdatedAt()
        );
    }
}
