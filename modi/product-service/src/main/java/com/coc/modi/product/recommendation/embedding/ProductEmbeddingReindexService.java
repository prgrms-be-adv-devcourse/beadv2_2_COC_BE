package com.coc.modi.product.recommendation.embedding;

import java.util.List;

import org.springframework.stereotype.Service;

import com.coc.modi.product.event.KafkaProductEmbeddingEventPublisher;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.product.domain.ProductStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductEmbeddingReindexService {
	
	private final ProductRepository productRepository;
	private final KafkaProductEmbeddingEventPublisher productEmbeddingEventPublisher;
	
	public int reindexAll() {
		
		List<Product> products = productRepository.findByStatusNot(ProductStatus.DELETE);
		for (Product product : products) {
			productEmbeddingEventPublisher.publishUpdate(product.getId());
		}
		return products.size();
	}
	
	public boolean reindexOne(Long productId) {
		
		if (productId == null) {
			return false;
		}
		
		return productRepository.findById(productId)
				.map(product -> {
					productEmbeddingEventPublisher.publishUpdate(product.getId());
					return true;
				})
				.orElse(false);
	}
}
