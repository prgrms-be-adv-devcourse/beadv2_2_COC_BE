package com.coc.modi.product.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.product.application.ProductService;
import com.coc.modi.product.application.dto.ProductCommand;
import com.coc.modi.product.application.dto.ProductInfo;
import com.coc.modi.product.application.dto.ProductListInfo;
import com.coc.modi.product.application.dto.ProductUpdateCommand;
import com.coc.modi.product.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    // 상품 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductListResponseDto>>> getProducts(Pageable pageable) {

        Page<ProductListInfo> page = service.getProducts(pageable);

        List<ProductListResponseDto> body = page.stream()
                .map(ProductListResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductDetail(@PathVariable("productId") Long productId) {

        ProductInfo info = service.getProductDetail(productId);

        return ResponseEntity.ok(ApiResponse.ok(ProductResponseDto.from(info)));
    }

    // 상품 이미지 등록
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageResponseDto>> uploadImage(@RequestPart("file") MultipartFile file) {

        String url = service.uploadImage(file);

        return ResponseEntity.ok(ApiResponse.ok(ImageResponseDto.from(url)));
    }

    // 상품 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(@RequestBody ProductRequestDto request) {

        // TODO: sellerId 연결
        Long sellerId = 0L;

        ProductCommand command = new ProductCommand(
                request.name(),
                request.description(),
                request.pricePerDay(),
                request.category(),
                request.images()
        );

        ProductInfo product = service.createProduct(sellerId, command);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ProductResponseDto.from(product)));
    }

    // 상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(@PathVariable("productId") Long productId,
            @RequestBody ProductUpdateRequestDto request) {

        ProductUpdateCommand command = new ProductUpdateCommand(
                request.name(),
                request.description(),
                request.pricePerDay(),
                request.category(),
                request.images()
        );

        ProductInfo product = service.updateProduct(productId, command);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ProductResponseDto.from(product)));
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
