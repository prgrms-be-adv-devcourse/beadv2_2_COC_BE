package com.coc.modi.admin.seller.application.dto;

import com.coc.modi.admin.seller.domain.SellerRegistrationStatus;

public record SellerRegistrationResponse(
		Long registrationId,
		Long memberId,
		String storeName,
		String bizRegNo,
		String storePhone,
		SellerRegistrationStatus status,
		Long approvedBy
) {
}
