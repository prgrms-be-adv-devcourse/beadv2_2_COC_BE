package com.coc.modi.product.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.product.application.ProductService;
import com.coc.modi.product.application.dto.*;
import com.coc.modi.product.presentation.dto.ProductRequestDto;
import com.coc.modi.product.presentation.dto.ProductUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    // 상품 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductListResponse>>> getProducts(
            @ModelAttribute ProductSearchCondition condition,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(service.searchProducts(condition, pageable)));
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetail(@PathVariable("productId") Long productId) {

        return ResponseEntity.ok(ApiResponse.ok(service.getProductDetail(productId)));
    }

    // 상품 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody ProductRequestDto request) {

        // TODO: sellerId 연결
        Long sellerId = 0L;

        ProductCommand command = new ProductCommand(
                request.name(),
                request.description(),
                request.pricePerDay(),
                request.category(),
                request.images()
        );

        return ResponseEntity.ok(ApiResponse.ok(service.createProduct(sellerId, command)));
    }

    // 상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable("productId") Long productId,
                                                                      @RequestBody ProductUpdateRequestDto request) {

        ProductUpdateCommand command = new ProductUpdateCommand(
                request.name(),
                request.description(),
                request.pricePerDay(),
                request.category(),
                request.images()
        );

        return ResponseEntity.ok(ApiResponse.ok(service.updateProduct(productId, command)));
    }

    // 상품 숨김
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> disableProduct(@PathVariable("productId") Long productId) {

        service.disableProduct(productId);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable("productId") Long productId) {

        service.deleteProduct(productId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
