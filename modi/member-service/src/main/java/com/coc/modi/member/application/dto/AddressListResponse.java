package com.coc.modi.member.application.dto;

import java.util.List;

public record AddressListResponse(
        List<AddressResponse> addressList
) {
    public static AddressListResponse from(List<AddressResponse> addressResponses) {

        return new AddressListResponse(addressResponses);
    }
}
