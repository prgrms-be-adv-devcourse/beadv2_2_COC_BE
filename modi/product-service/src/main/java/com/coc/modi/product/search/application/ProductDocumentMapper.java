package com.coc.modi.product.search.application;

import org.springframework.stereotype.Component;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductImageRepository;
import com.coc.modi.product.search.domain.ProductDocument;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductDocumentMapper {

	private final ProductImageRepository productImageRepository;

	public ProductDocument toDocument(Product product) {
		if (product == null) {
			return null;
		}
		String thumbnailUrl = resolveThumbnailUrl(product);
		return ProductDocument.from(product, thumbnailUrl);
	}

	private String resolveThumbnailUrl(Product product) {
		Long thumbnailId = product.getThumbnailImageId();
		if (thumbnailId == null) {
			return null;
		}
		return productImageRepository.findById(thumbnailId)
				.map(ProductImage::getUrl)
				.orElse(null);
	}
}
