package com.coc.modi.rental.rental.presentation;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.rental.rental.application.RentalQueryService;
import com.coc.modi.rental.rental.infrastructure.client.dto.UnavailableProductsRequest;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalInternalSearchCondition;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalItemInfo;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalItemInfoListResponse;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalItemSellerResponse;
import com.coc.modi.rental.rental.infrastructure.client.dto.UnavailableProductsResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/rentals")
@Validated
public class RentalInternalController {
	
	private final RentalQueryService queryService;

	@GetMapping
	public RentalItemInfoListResponse getRentalItemList(@Valid @ModelAttribute RentalInternalSearchCondition condition,
													  Pageable pageable) {
		
		return queryService.getRentalItemList(condition, pageable);
	}

	@GetMapping("/items/{rentalItemId}/info")
	public RentalItemInfo getRentalItem(@PathVariable Long rentalItemId) {
		return queryService.getRentalItemInfo(rentalItemId);
	}

	@GetMapping("/items/{rentalItemId}")
	public RentalItemSellerResponse getRentalItemSeller(@PathVariable Long rentalItemId) {

		return queryService.getRentalItemSellerInfo(rentalItemId);
	}

	@PostMapping("/unavailable-products")
	public UnavailableProductsResponse getUnavailableProducts(@Valid @RequestBody UnavailableProductsRequest unavailableProductsRequest) {
		
		return queryService.getUnavailableProducts(unavailableProductsRequest);
	}
}
