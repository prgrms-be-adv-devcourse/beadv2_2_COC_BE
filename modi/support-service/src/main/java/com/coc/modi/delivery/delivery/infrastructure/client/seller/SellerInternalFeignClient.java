package com.coc.modi.delivery.delivery.infrastructure.client.seller;

import com.coc.modi.delivery.delivery.infrastructure.client.seller.dto.SellerIdResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
		contextId = "deliverySellerClient",
		name = "seller-service",
		url = "${seller-service.url}",
		path = "/internal/sellers"
)
public interface SellerInternalFeignClient {
	
	@GetMapping("/by-member/{memberId}")
	SellerIdResponse getSellerByMember(@PathVariable Long memberId);
}
