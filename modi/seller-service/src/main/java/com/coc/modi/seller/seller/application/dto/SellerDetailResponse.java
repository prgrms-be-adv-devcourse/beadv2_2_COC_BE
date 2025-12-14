package com.coc.modi.seller.seller.application.dto;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerStatus;

import java.time.LocalDateTime;

public record SellerDetailResponse(
        Long sellerId,
        Long memberId,
        String storeName,
        String bizRegNo,
        String storePhone,
        SellerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SellerDetailResponse from(Seller seller) {
        
        return new SellerDetailResponse(
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
