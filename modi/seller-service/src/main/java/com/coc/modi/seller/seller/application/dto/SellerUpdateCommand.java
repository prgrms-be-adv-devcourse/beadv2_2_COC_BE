package com.coc.modi.seller.seller.application.dto;

import com.coc.modi.seller.seller.domain.SellerStatus;


public record SellerUpdateCommand(
        String storeName,
        String bizRegNo,
        String storePhone,
        SellerStatus status
) {
}
