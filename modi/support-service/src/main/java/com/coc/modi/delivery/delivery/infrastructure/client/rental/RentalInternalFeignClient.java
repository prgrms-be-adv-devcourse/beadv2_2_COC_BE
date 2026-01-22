package com.coc.modi.delivery.delivery.infrastructure.client.rental;

import com.coc.modi.delivery.delivery.infrastructure.client.rental.dto.RentalItemSellerResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
		contextId = "deliveryRentalClient",
		name = "rental-service",
		url = "${rental-service.url}",
		path = "/internal/rentals"
)
public interface RentalInternalFeignClient {
	
	@GetMapping("/items/{rentalItemId}")
	RentalItemSellerResponse getRentalItemSeller(@PathVariable Long rentalItemId);
}
