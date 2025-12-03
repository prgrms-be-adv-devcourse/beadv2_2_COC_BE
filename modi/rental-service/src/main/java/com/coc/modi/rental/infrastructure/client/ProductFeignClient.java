package com.coc.modi.rental.infrastructure.client;

import com.coc.modi.rental.infrastructure.client.dto.ProductPriceResponseDto;
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

    @GetMapping("/prices")
    List<ProductPriceResponseDto> getProductPrices(
            @RequestParam("productIds") List<Long> productIds
    );
}
