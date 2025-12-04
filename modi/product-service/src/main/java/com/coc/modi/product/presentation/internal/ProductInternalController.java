package com.coc.modi.product.presentation.internal;

import com.coc.modi.product.application.ProductService;
import com.coc.modi.product.domain.Product;
import com.coc.modi.product.presentation.dto.ProductBulkResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/products")
public class ProductInternalController {

    private final ProductService productService;

    @PostMapping("/bulk")
    public ResponseEntity<List<ProductBulkResponseDto>> getProductsBulk(
            @RequestBody List<Long> productIds
    ) {
        List<Product> products = productService.getProductsByIds(productIds);

        List<ProductBulkResponseDto> result = products.stream()
                .map(ProductBulkResponseDto::from)
                .toList();

        return ResponseEntity.ok(result);
    }
}
