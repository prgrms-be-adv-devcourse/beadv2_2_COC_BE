package com.coc.modi.rental.rental.presentation;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.rental.rental.application.RentalQueryService;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalInternalSearchCondition;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalItemInfoListResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/rentals")
public class RentalInternalController {
	
	private final RentalQueryService queryService;

	@GetMapping
	public RentalItemInfoListResponse getRentalItemList(@ModelAttribute RentalInternalSearchCondition condition,
															  Pageable pageable) {
		
		return queryService.getRentalItemList(condition, pageable);
	}
}
