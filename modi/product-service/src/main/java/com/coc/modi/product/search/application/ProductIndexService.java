package com.coc.modi.product.search.application;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductImageRepository;
import com.coc.modi.product.search.domain.ProductDocument;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductIndexService {
	
	private final ProductSearchPort searchPort;
	private final ProductImageRepository imageRepository;
	
	public void index(Product product) {
		
		String thumbnailUrl = resolveThumbnailUrl(product);
		ProductDocument doc = ProductDocument.from(product, thumbnailUrl);
		
		searchPort.index(doc);
	}
	
	public String resolveThumbnailUrl(Product product) {
		
		Long thumbnailId = product.getThumbnailImageId();
		
		if (thumbnailId == null) {
			
			return null;
		}
		
		return imageRepository.findById(thumbnailId)
				.map(ProductImage::getUrl)
				.orElse(null);
	}
}
