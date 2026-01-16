package com.coc.modi.delivery.delivery.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryUpdateRequest(
		@NotBlank(message = "carrierCode는 필수입니다.")
		@Size(max = 30, message = "carrierCode는 30자를 넘을 수 없습니다.")
		String carrierCode,
		
		@NotBlank(message = "trackingNumber는 필수입니다.")
		@Size(max = 50, message = "trackingNumber는 50자를 넘을 수 없습니다.")
		String trackingNumber
) {
}
