package com.coc.modi.seller.seller.presentation.dto;

import com.coc.modi.seller.seller.application.dto.SellerCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerCreateRequest(
        @NotBlank
        @Size(max = 50)
        String storeName,
        @Size(max = 20)
        String bizRegNo,
        @Size(max = 20)
        String storePhone
) {

    public SellerCreateCommand toCommand(Long memberId) {
        
        return new SellerCreateCommand(memberId, storeName, bizRegNo, storePhone);
    }
}
