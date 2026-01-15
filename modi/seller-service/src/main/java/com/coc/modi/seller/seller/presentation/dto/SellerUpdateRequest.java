package com.coc.modi.seller.seller.presentation.dto;

import com.coc.modi.seller.seller.application.dto.SellerUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record SellerUpdateRequest(
        @NotBlank
        @Size(max = 50)
        String storeName,
        @Size(max = 20)
        String bizRegNo,
        @Size(max = 20)
        String storePhone
) {

    public SellerUpdateCommand toCommand() {
        
        return new SellerUpdateCommand(storeName, bizRegNo, storePhone);
    }
}
