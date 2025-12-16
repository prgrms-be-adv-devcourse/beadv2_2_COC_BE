package com.coc.modi.member.member.presentation.dto;

import com.coc.modi.member.member.application.dto.AddressCreateCommand;
import com.coc.modi.member.member.domain.AddressType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressCreateRequest(
		@NotBlank
		@Size(max = 30)
		String addressLabel,
		
		@NotBlank
		@Size(max = 20)
		String recipientName,
		
		@NotBlank
		@Pattern(
				regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
				message = "올바른 휴대폰 번호 형식이 아닙니다"
		)
		String recipientPhone,
		
		@NotNull
		AddressType type,
		
		@NotBlank
		@Pattern(
				regexp = "^\\d{5}$",
				message = "우편번호는 5자리 숫자여야 합니다"
		)
		String postcode,
		
		@NotBlank
		@Size(max = 100)
		String roadAddress,
		
		@NotBlank
		@Size(max = 100)
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
