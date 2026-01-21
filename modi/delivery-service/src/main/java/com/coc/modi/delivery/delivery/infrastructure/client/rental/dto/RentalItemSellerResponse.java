package com.coc.modi.delivery.delivery.infrastructure.client.rental.dto;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.delivery.delivery.exception.DeliveryException;

public record RentalItemSellerResponse(
		Long rentalItemId,
		Long sellerId,
		Long memberId
) {
	public RentalItemSellerResponse {
		
		if (rentalItemId == null || sellerId == null || memberId == null) {
			throw new DeliveryException(ErrorCode.DELIVERY_INVALID_INPUT, "렌탈 아이템/판매자/회원 정보는 필수입니다.");
		}
	}
}
