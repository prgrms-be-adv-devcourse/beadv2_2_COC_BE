package com.coc.modi.member.application.dto;

import com.coc.modi.member.domain.AddressType;

public record AddressUpdateCommand(
        Long memberId,
        Long addressId,
        String addressLabel,
        String recipientName,
        String recipientPhone,
        AddressType type,
        String postcode,
        String roadAddress,
        String detailAddress,
        boolean isDefault
) {
}
