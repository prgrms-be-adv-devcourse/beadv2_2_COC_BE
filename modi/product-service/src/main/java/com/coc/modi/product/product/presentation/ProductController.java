package com.coc.modi.product.product.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.product.product.application.ProductService;
import com.coc.modi.product.product.application.dto.ProductCommand;
import com.coc.modi.product.product.application.dto.ProductResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.application.dto.ProductUpdateCommand;
import com.coc.modi.product.product.presentation.dto.ProductRequest;
import com.coc.modi.product.product.presentation.dto.ProductUpdateRequest;
import com.coc.modi.product.search.domain.ProductSortType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    // 상품 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<ProductScrollResponse>> getProducts(
            @ModelAttribute ProductSearchCondition condition,
			@RequestParam(name = "cursor", required = false) String cursor,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestParam(name = "sortType", defaultValue = "LATEST") ProductSortType sortType) {

        return ResponseEntity.ok(ApiResponse.ok(service.searchProducts(condition, cursor, size, sortType)));
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetail(@PathVariable("productId") Long productId) {

        return ResponseEntity.ok(ApiResponse.ok(service.getProductDetail(productId)));
    }

    // 상품 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(Authentication authentication, @RequestBody ProductRequest request) {
		
		Long memberId = (Long) authentication.getPrincipal();

        ProductCommand command = new ProductCommand(
                request.name(),
                request.description(),
                request.pricePerDay(),
                request.category(),
                request.images()
        );

        return ResponseEntity.ok(ApiResponse.ok(service.createProduct(memberId, command)));
    }

    // 상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(Authentication authentication,
																	  @PathVariable("productId") Long productId,
                                                                      @RequestBody ProductUpdateRequest request) {
		
		Long memberId = (Long) authentication.getPrincipal();

        ProductUpdateCommand command = new ProductUpdateCommand(
                request.name(),
                request.description(),
                request.pricePerDay(),
                request.category(),
                request.images()
        );

        return ResponseEntity.ok(ApiResponse.ok(service.updateProduct(memberId, productId, command)));
    }

    // 상품 숨김
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> disableProduct(Authentication authentication,
														 @PathVariable("productId") Long productId) {
		
		Long memberId = (Long) authentication.getPrincipal();

        service.disableProduct(memberId, productId);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(Authentication authentication,
														@PathVariable("productId") Long productId) {
		
		Long memberId = (Long) authentication.getPrincipal();

        service.deleteProduct(memberId, productId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
