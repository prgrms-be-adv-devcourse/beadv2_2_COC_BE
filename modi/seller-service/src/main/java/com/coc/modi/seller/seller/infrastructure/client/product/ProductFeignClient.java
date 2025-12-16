package com.coc.modi.seller.seller.infrastructure.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.coc.modi.seller.seller.infrastructure.client.product.dto.ProductSummaryResponse;

@FeignClient(
        name = "product-service",
        url = "${product-service.url}",
        path = "/internal/products"
)
public interface ProductFeignClient {

    @GetMapping("/{productId}")
    ProductSummaryResponse getProduct(@PathVariable("productId") Long productId);
}
