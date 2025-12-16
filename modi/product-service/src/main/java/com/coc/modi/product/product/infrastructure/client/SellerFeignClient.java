package com.coc.modi.product.product.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.coc.modi.product.product.application.dto.SellerResponse;

@FeignClient(
		name = "seller-service",
		url = "${seller-service.url}",
		path = "/internal/sellers"
)
public interface SellerFeignClient {
	
	@GetMapping("/by-member/{memberId}")
	SellerResponse getSellerIdByMemberId(@PathVariable("memberId") Long memberId);
}
