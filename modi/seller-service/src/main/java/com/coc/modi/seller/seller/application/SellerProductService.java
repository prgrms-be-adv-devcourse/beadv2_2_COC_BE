package com.coc.modi.seller.seller.application;

import org.springframework.stereotype.Service;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.seller.seller.infrastructure.client.product.ProductFeignClient;
import com.coc.modi.seller.seller.infrastructure.client.product.dto.ProductSummaryResponse;
import com.coc.modi.seller.seller.exception.SellerException;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerProductService {

    private final ProductFeignClient productFeignClient;

    public ProductSummaryResponse getProductSummary(Long productId) {

        try {
            return productFeignClient.getProduct(productId);
        } catch (FeignException ex) {
            log.warn("상품 서비스 호출 실패 productId={}", productId, ex);
            throw new SellerException(ErrorCode.INTERNAL_ERROR, "상품 서비스 호출에 실패했습니다.");
        }
    }
}
