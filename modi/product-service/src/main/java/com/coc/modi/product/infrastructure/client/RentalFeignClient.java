package com.coc.modi.product.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.coc.modi.product.application.dto.RentalResponse;
import com.coc.modi.product.presentation.dto.RentalRequest;

@FeignClient(
		name = "rental-service",
		url = "${rental-service.url}",
		path = "/internal/rentals"
)
public interface RentalFeignClient {
	
	@PostMapping("/unavailable-products")
	RentalResponse unavailableProducts(@RequestBody RentalRequest rentalRequest);
}
