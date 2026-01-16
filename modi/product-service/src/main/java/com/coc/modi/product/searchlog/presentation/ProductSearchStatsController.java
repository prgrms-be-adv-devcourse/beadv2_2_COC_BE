package com.coc.modi.product.searchlog.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.product.searchlog.application.ProductSearchStatsService;
import com.coc.modi.product.searchlog.presentation.dto.PopularKeywordResponse;
import com.coc.modi.product.searchlog.presentation.dto.PopularProductResponse;
import com.coc.modi.product.viewlog.application.ProductViewStatsService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductSearchStatsController {

	private final ProductSearchStatsService productSearchStatsService;
	private final ProductViewStatsService productViewStatsService;

	@GetMapping("/popular-keywords")
	public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> getPopularKeywords(
			@RequestParam(name = "size", defaultValue = "10") Integer size,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		return ResponseEntity.ok(ApiResponse.ok(productSearchStatsService.getPopularKeywords(size, startDate, endDate)));
	}

	@GetMapping("/popular-products")
	public ResponseEntity<ApiResponse<List<PopularProductResponse>>> getPopularProducts(
			@RequestParam(name = "size", defaultValue = "10") Integer size,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		return ResponseEntity.ok(ApiResponse.ok(productViewStatsService.getPopularProducts(size, startDate, endDate)));
	}
}
