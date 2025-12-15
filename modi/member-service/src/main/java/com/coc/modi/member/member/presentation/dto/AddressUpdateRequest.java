package com.coc.modi.member.member.presentation.dto;

import com.coc.modi.member.member.application.dto.AddressUpdateCommand;
import com.coc.modi.member.member.domain.AddressType;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressUpdateRequest(
		@Size(max = 30)
		String addressLabel,
		
		@Size(max = 20)
		String recipientName,
		
		@Pattern(
				regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
				message = "올바른 휴대폰 번호 형식이 아닙니다"
		)
		String recipientPhone,
		
		AddressType type,
		
		@Pattern(
				regexp = "^\\d{5}$",
				message = "우편번호는 5자리 숫자여야 합니다"
		)
		String postcode,
		
		@Size(max = 100)
		String roadAddress,
		
		@Size(max = 100)
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
