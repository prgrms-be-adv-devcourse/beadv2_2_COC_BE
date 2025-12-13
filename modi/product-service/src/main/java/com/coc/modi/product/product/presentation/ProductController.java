package com.coc.modi.product.product.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.product.product.application.ProductService;
import com.coc.modi.product.product.application.ProductStatusService;
import com.coc.modi.product.product.application.dto.ProductCreateCommand;
import com.coc.modi.product.product.application.dto.ProductDetailResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.application.dto.ProductUpdateCommand;
import com.coc.modi.product.product.presentation.dto.ProductCreateRequest;
import com.coc.modi.product.product.presentation.dto.ProductUpdateRequest;
import com.coc.modi.product.search.domain.ProductSortType;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
	
	// 상품 상세 조회
	@GetMapping("/{productId}")
	public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable("productId") Long productId) {
		
		return ResponseEntity.ok(ApiResponse.ok(productService.getProductDetail(productId)));
	}
	
	// 상품 등록
	@PostMapping
	public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(Authentication authentication,
																			@Valid @RequestBody ProductCreateRequest request) {
		
		Long memberId = (Long)authentication.getPrincipal();
		
		ProductCreateCommand command = ProductCreateCommand.toCommand(memberId, request);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(productService.createProduct(command)));
	}
	
	// 상품 수정
	@PutMapping("/{productId}")
	public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(Authentication authentication,
																			@PathVariable("productId") Long productId,
																			@Valid @RequestBody ProductUpdateRequest request) {
		
		Long memberId = (Long)authentication.getPrincipal();
		
		ProductUpdateCommand command = ProductUpdateCommand.toCommand(memberId, productId, request);
		
		return ResponseEntity.ok(ApiResponse.ok(productService.updateProduct(command)));
	}
	
	// 상품 활성화
	@PatchMapping("/{productId}/active")
	public ResponseEntity<ApiResponse<Void>> activeProduct(Authentication authentication,
														   @PathVariable("productId") Long productId) {
		
		Long memberId = (Long)authentication.getPrincipal();
		
		productStatusService.activeProduct(memberId, productId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	// 상품 숨김
	@PatchMapping("/{productId}/inactive")
	public ResponseEntity<ApiResponse<Void>> disableProduct(Authentication authentication,
														 @PathVariable("productId") Long productId) {
		
		Long memberId = (Long)authentication.getPrincipal();
		
		productStatusService.disableProduct(memberId, productId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	// 상품 삭제
	@DeleteMapping("/{productId}")
	public ResponseEntity<ApiResponse<Void>> deleteProduct(Authentication authentication,
														@PathVariable("productId") Long productId) {
		
		Long memberId = (Long)authentication.getPrincipal();
		
		productStatusService.deleteProduct(memberId, productId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
}
