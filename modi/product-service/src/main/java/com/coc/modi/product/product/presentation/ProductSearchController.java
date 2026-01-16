package com.coc.modi.product.product.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.product.product.application.ProductService;
import com.coc.modi.product.product.application.dto.ProductListResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.search.domain.ProductSortType;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductSearchController {
	
	private final ProductService productService;
	
	// 상품 목록 조회
	@GetMapping
	public ResponseEntity<ApiResponse<ProductScrollResponse>> getProducts(
			@AuthenticationPrincipal CustomMember member,
			@ModelAttribute ProductSearchCondition condition,
			@RequestParam(name = "cursor", required = false) String cursor,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestParam(name = "sortType", defaultValue = "LATEST") ProductSortType sortType) {
		
		Long memberId = member != null ? member.memberId() : null;
		
		return ResponseEntity.ok(ApiResponse.ok(productService.searchProducts(condition, cursor, size, sortType, memberId)));
	}
	
	// 판매자 상품 목록 조회
	@GetMapping("/seller")
	public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getSellerProducts(
			@AuthenticationPrincipal CustomMember member,
			@PageableDefault(
					size = 20,
					sort = "createdAt",
					direction = Sort.Direction.DESC
			) Pageable pageable) {
		
		return ResponseEntity.ok(ApiResponse.ok(productService.searchSellerProducts(member.memberId(), pageable)));
	}
}
