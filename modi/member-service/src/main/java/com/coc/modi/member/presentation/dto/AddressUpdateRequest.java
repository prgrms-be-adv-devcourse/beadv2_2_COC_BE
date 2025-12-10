package com.coc.modi.member.presentation.dto;

import com.coc.modi.member.application.dto.AddressUpdateCommand;
import com.coc.modi.member.domain.AddressType;

public record AddressUpdateRequest(
        String addressLabel,
        String recipientName,
        String recipientPhone,
        AddressType type,
        String postcode,
        String roadAddress,
        String detailAddress,
        boolean isDefault
) {
    public AddressUpdateCommand toCommand(Long memberId, Long addressId) {

        return new AddressUpdateCommand(
                memberId,
                addressId,
                addressLabel,
                recipientName,
                recipientPhone,
                type,
                postcode,
                roadAddress,
                detailAddress,
                isDefault
        );
    }
}
