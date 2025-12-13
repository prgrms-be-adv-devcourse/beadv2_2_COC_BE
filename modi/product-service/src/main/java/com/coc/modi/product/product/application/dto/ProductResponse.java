package com.coc.modi.product.product.application.dto;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
		Long id,
		Long sellerId,
		String name,
		String description,
		BigDecimal pricePerDay,
		ProductStatus status,
		ProductCategory category,
		Long thumbnailImageId,
		List<ImageInfo> images
) {
	public static ProductResponse from(Product product) {
		
		List<ImageInfo> images = product.getImages().stream()
				.map(ImageInfo::from)
				.toList();
		
		return new ProductResponse(
				product.getId(),
				product.getSellerId(),
				product.getName(),
				product.getDescription(),
				product.getPricePerDay(),
				product.getStatus(),
				product.getCategory(),
				product.getThumbnailImageId(),
				images
		);
	}
	
	public record ImageInfo(
			Long id,
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
