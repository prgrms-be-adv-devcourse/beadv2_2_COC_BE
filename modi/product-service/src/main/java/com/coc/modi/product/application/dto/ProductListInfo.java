package com.coc.modi.product.application.dto;

import com.coc.modi.product.domain.Product;
import com.coc.modi.product.domain.ProductImage;
import com.coc.modi.product.domain.ProductStatus;

import java.math.BigDecimal;

public record ProductListInfo(
        Long id,
        String name,
        BigDecimal pricePerDay,
        ProductStatus status,
        Long sellerId,
        String thumbnailUrl
) {
    public static ProductListInfo from(Product product) {
        String thumb = product.getImages().stream()
                .filter(ProductImage::getIsThumbnail)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(null);

        return new ProductListInfo(
                product.getId(),
                product.getName(),
                product.getPricePerDay(),
                product.getStatus(),
                product.getSellerId(),
                thumb
        );
    }
}