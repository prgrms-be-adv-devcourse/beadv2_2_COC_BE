package com.coc.modi.product.product.presentation.internal;

import com.coc.modi.product.product.application.ProductService;
import com.coc.modi.product.product.application.dto.ProductBulkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/products")
public class ProductInternalController {

    private final ProductService productService;

    @PostMapping("/bulk")
    public List<ProductBulkResponse> getProductsBulk(
            @RequestBody List<Long> productIds
    ) {

        return productService.getProductsByIds(productIds);
    }
}
