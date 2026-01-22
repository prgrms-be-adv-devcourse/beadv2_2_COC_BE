package com.coc.modi.seller.seller.application.dto;

import com.coc.modi.seller.seller.registration.domain.SellerRegistration;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationStatus;

public record SellerRegistrationResponse(
		Long registrationId,
		Long memberId,
		String storeName,
		String bizRegNo,
		String storePhone,
		SellerRegistrationStatus status,
		Long approvedBy
) {

	public static SellerRegistrationResponse from(SellerRegistration registration) {

		return new SellerRegistrationResponse(
				registration.getId(),
				registration.getMemberId(),
				registration.getStoreName(),
				registration.getBizRegNo(),
				registration.getStorePhone(),
				registration.getStatus(),
				registration.getApprovedBy()
		);
	}
}
