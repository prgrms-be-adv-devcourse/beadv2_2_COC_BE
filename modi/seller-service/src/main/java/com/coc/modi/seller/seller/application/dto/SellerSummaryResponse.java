package com.coc.modi.seller.seller.application.dto;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerStatus;

public record SellerSummaryResponse(
        Long sellerId,
        String storeName,
        SellerStatus status
) {

    public static SellerSummaryResponse from(Seller seller) {
        
        return new SellerSummaryResponse(
                seller.getId(),
                seller.getStoreName(),
                seller.getStatus()
        );
    }
}
