package com.coc.modi.product.application.dto;


import com.coc.modi.product.domain.Product;
import com.coc.modi.product.domain.ProductCategory;
import com.coc.modi.product.domain.ProductImage;
import com.coc.modi.product.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record ProductInfo(
        Long id,
        Long sellerId,
        String name,
        String description,
        BigDecimal pricePerDay,
        ProductStatus status,
        ProductCategory category,
        List<ImageInfo> images
) {
    public static ProductInfo from(Product product) {
        List<ImageInfo> images = product.getImages().stream()
                .map(ImageInfo::from)
                .toList();

        return new ProductInfo(
                product.getId(),
                product.getSellerId(),
                product.getName(),
                product.getDescription(),
                product.getPricePerDay(),
                product.getStatus(),
                product.getCategory(),
                images
        );
    }

    public record ImageInfo(
            Long id,
            String url,
            int ordering,
            boolean isThumbnail
    ) {
        public static ImageInfo from(ProductImage image) {
            return new ImageInfo(
                    image.getId(),
                    image.getUrl(),
                    image.getOrdering(),
                    image.getIsThumbnail()
            );
        }
    }
}