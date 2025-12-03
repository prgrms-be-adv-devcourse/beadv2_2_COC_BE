package com.coc.modi.rental.infrastructure.client;

import com.coc.modi.rental.infrastructure.client.dto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "product-service",
        url = "${product-service.url}",
        path = "/internal/products"
)
public interface ProductFeignClient {

    @GetMapping("/bulk")
    List<ProductResponseDto> getProducts(
            @RequestParam("productIds") List<Long> productIds
    );

    default ProductResponseDto getProducts(Long productId) {

        List<ProductResponseDto> products = getProducts(List.of(productId));

        if (products.isEmpty()) {

            throw new IllegalArgumentException("상품이 존재하지 않음: " + products);
        }

        return products.get(0);
    }
}
