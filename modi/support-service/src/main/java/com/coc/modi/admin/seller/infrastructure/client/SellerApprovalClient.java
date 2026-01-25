package com.coc.modi.admin.seller.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.coc.modi.admin.seller.application.dto.SellerRegistrationResponse;
import com.coc.modi.admin.seller.infrastructure.client.dto.SellerRegistrationPageRequest;
import com.coc.modi.admin.seller.infrastructure.client.dto.SellerRegistrationPageResponse;

@FeignClient(
		contextId = "supportSellerApprovalClient",
		name = "seller-service",
		url = "${seller-service.url}",
		path = "/internal/sellers"
)
public interface SellerApprovalClient {

	@PatchMapping("/{memberId}/approve")
	SellerRegistrationResponse approveSeller(
			@PathVariable("memberId") Long memberId,
			@RequestParam("approvedBy") Long approvedBy
	);

	@PatchMapping("/{memberId}/reject")
	SellerRegistrationResponse rejectSeller(@PathVariable("memberId") Long memberId);

	@GetMapping("/registrations")
	SellerRegistrationPageResponse getRegistrations(@SpringQueryMap SellerRegistrationPageRequest request);
}
