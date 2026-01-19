package com.coc.modi.product.product.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
	
	Optional<Product> findByIdAndStatusNot(Long id, ProductStatus status);
	
	Optional<Product> findById(Long id);
	
	Page<Product> findBySellerIdAndStatusNot(Long sellerId, ProductStatus status, Pageable pageable);
	
	Product saveAndFlush(Product product);
	
	void flush();
	
	List<Product> findByIdIn(List<Long> productIds);

	List<Product> findByStatusNotAndModerationStatus(ProductStatus status, ProductModerationStatus moderationStatus);
}
