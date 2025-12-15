package com.coc.modi.rental.rental.infrastructure.client;

import com.coc.modi.rental.rental.infrastructure.client.dto.SellerInfoResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
		name = "seller-service",
		url = "${seller-service.url}",
		path = "/internal/sellers"
)
public interface SellerFeignClient {
	
	@GetMapping("/{sellerId}")
	SellerInfoResponse getSellerInfo(@PathVariable Long sellerId);
}
