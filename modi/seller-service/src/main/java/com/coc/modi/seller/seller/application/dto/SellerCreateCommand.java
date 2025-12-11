package com.coc.modi.seller.seller.application.dto;


public record SellerCreateCommand(
        Long memberId,
        String storeName,
        String bizRegNo,
        String storePhone
) {
}
