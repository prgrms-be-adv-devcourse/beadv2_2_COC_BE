package com.coc.modi.member.member.application.dto;

import com.coc.modi.member.member.domain.Address;
import com.coc.modi.member.member.domain.AddressType;

public record AddressResponse(
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
    public static AddressResponse from(Address address) {

        return new AddressResponse(
				address.getId(),
                address.getAddressLabel(),
                address.getRecipientName(),
                address.getRecipientPhone(),
                address.getType(),
                address.getPostcode(),
                address.getRoadAddress(),
                address.getDetailAddress(),
                address.isDefault()
        );
    }
}
