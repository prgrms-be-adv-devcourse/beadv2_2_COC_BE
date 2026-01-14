package com.coc.modi.review.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.coc.modi.review.infrastructure.client.dto.RentalItemInfo;

@FeignClient(
		name = "rental-service",
		url = "${rental-service.url}",
		path = "/internal/rentals"
)
public interface RentalFeignClient {
	
	@GetMapping("/items/{rentalItemId}")
	RentalItemInfo getRentalItem(@PathVariable("rentalItemId") Long rentalItemId);
}
