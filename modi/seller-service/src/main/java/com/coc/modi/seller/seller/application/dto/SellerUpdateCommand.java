package com.coc.modi.seller.seller.application.dto;

public record SellerUpdateCommand(
        String storeName,
        String bizRegNo,
        String storePhone
) {
}
