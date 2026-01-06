package com.coc.modi.product.product.application.dto;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

import java.util.Map;

public record ProductDetailResponse(
		Long productId,
		Long sellerId,
		String name,
		String description,
		BigDecimal pricePerDay,
		ProductStatus status,
		ProductCategory category,
		Long thumbnailImageId,
		Map<String, String> specs,
		List<ImageInfo> images
) {
	public static ProductDetailResponse from(Product product) {
		
		List<ImageInfo> images = product.getImages().stream()
				.map(ImageInfo::from)
				.toList();
		
		return new ProductDetailResponse(
				product.getId(),
				product.getSellerId(),
				product.getName(),
				product.getDescription(),
				product.getPricePerDay(),
				product.getStatus(),
				product.getCategory(),
				product.getThumbnailImageId(),
				product.getSpecs(),
				images
		);
	}
	
	public record ImageInfo(
			Long imageId,
			String url,
			int ordering
	) {
		public static ImageInfo from(ProductImage image) {
			
			return new ImageInfo(
					image.getId(),
					image.getUrl(),
					image.getOrdering()
			);
		}
	}
}
