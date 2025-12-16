package com.coc.modi.product.product.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.product.product.application.ProductService;
import com.coc.modi.product.product.application.ProductStatusService;
import com.coc.modi.product.product.application.dto.ProductCreateCommand;
import com.coc.modi.product.product.application.dto.ProductDetailResponse;
import com.coc.modi.product.product.application.dto.ProductListResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.application.dto.ProductUpdateCommand;
import com.coc.modi.product.product.presentation.dto.ProductCreateRequest;
import com.coc.modi.product.product.presentation.dto.ProductUpdateRequest;
import com.coc.modi.product.search.domain.ProductSortType;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
	
	private final ProductService productService;
	private final ProductStatusService productStatusService;
	
	// 상품 목록 조회
	@GetMapping
	public ResponseEntity<ApiResponse<ProductScrollResponse>> getProducts(
			@ModelAttribute ProductSearchCondition condition,
			@RequestParam(name = "cursor", required = false) String cursor,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestParam(name = "sortType", defaultValue = "LATEST") ProductSortType sortType) {
		
		return ResponseEntity.ok(ApiResponse.ok(productService.searchProducts(condition, cursor, size, sortType)));
	}
	
	// 판매자 상품 목록 조회
	@GetMapping("/seller")
	public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getSellerProducts(@AuthenticationPrincipal CustomMember member,
																					@PageableDefault(
																						size = 20,
																						sort = "createdAt",
																						direction = Sort.Direction.DESC
																				) Pageable pageable) {
		
		return ResponseEntity.ok(ApiResponse.ok(productService.searchSellerProducts(member.memberId(), pageable)));
	}
	
	// 상품 상세 조회
	@GetMapping("/{productId}")
	public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@AuthenticationPrincipal CustomMember member,
																			   @PathVariable("productId") Long productId) {
		
		return ResponseEntity.ok(ApiResponse.ok(productService.getProductDetail(member.memberId(), productId)));
	}
	
	// 상품 등록
	@PostMapping
	public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(@AuthenticationPrincipal CustomMember member,
																			@Valid @RequestBody ProductCreateRequest request) {
		
		ProductCreateCommand command = ProductCreateCommand.toCommand(member.memberId(), request);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(productService.createProduct(command)));
	}
	
	// 상품 수정
	@PutMapping("/{productId}")
	public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(@AuthenticationPrincipal CustomMember member,
																			@PathVariable("productId") Long productId,
																			@Valid @RequestBody ProductUpdateRequest request) {
		
		ProductUpdateCommand command = ProductUpdateCommand.toCommand(member.memberId(), productId, request);
		
		return ResponseEntity.ok(ApiResponse.ok(productService.updateProduct(command)));
	}
	
	// 상품 활성화
	@PatchMapping("/{productId}/active")
	public ResponseEntity<ApiResponse<Void>> activeProduct(@AuthenticationPrincipal CustomMember member,
														   @PathVariable("productId") Long productId) {
		
		productStatusService.activeProduct(member.memberId(), productId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	// 상품 숨김
	@PatchMapping("/{productId}/inactive")
	public ResponseEntity<ApiResponse<Void>> disableProduct(@AuthenticationPrincipal CustomMember member,
														    @PathVariable("productId") Long productId) {
		
		productStatusService.disableProduct(member.memberId(), productId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	// 상품 삭제
	@DeleteMapping("/{productId}")
	public ResponseEntity<ApiResponse<Void>> deleteProduct(@AuthenticationPrincipal CustomMember member,
														   @PathVariable("productId") Long productId) {
		
		productStatusService.deleteProduct(member.memberId(), productId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
}
