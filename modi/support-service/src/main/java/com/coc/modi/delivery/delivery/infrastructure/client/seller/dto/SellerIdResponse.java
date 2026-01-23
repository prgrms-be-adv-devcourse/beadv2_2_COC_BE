package com.coc.modi.delivery.delivery.infrastructure.client.seller.dto;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.delivery.delivery.exception.DeliveryException;

public record SellerIdResponse(
		Long sellerId,
		Long memberId
) {
	public SellerIdResponse {
		
		if (sellerId == null || memberId == null) {
			throw new DeliveryException(ErrorCode.DELIVERY_INVALID_INPUT, "판매자/회원 정보는 필수입니다.");
		}
	}
}
