package com.coc.modi.seller.seller.application;

import org.springframework.stereotype.Service;

import com.coc.modi.seller.seller.infrastructure.client.product.ProductFeignClient;
import com.coc.modi.seller.seller.infrastructure.client.product.dto.ProductSummaryResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerProductService {

    private final ProductFeignClient productFeignClient;

    public ProductSummaryResponse getProductSummary(Long productId) {

        return productFeignClient.getProduct(productId);
    }
}
