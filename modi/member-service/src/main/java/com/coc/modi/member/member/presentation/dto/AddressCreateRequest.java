package com.coc.modi.member.member.presentation.dto;

import com.coc.modi.member.member.application.dto.AddressCreateCommand;
import com.coc.modi.member.member.domain.AddressType;

public record AddressCreateRequest(
        String addressLabel,
        String recipientName,
        String recipientPhone,
        AddressType type,
        String postcode,
        String roadAddress,
        String detailAddress,
        boolean isDefault
) {
    public AddressCreateCommand toCommand(Long memberId) {

        return new AddressCreateCommand(
                memberId,
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
