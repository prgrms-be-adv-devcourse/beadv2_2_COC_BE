package com.coc.modi.product.product.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
	
	Optional<Product> findNonDeletedById(Long id);
	
	Optional<Product> findById(Long id);
	
	Page<Product> findNonDeletedBySellerId(Long sellerId, Pageable pageable);

	Page<Product> findNonDeletedByModerationStatus(ProductModerationStatus moderationStatus, Pageable pageable);
	
	Product saveAndFlush(Product product);
	
	void flush();
	
	List<Product> findByIdIn(List<Long> productIds);

	List<Product> findNonDeletedByModerationStatus(ProductModerationStatus moderationStatus);
}
