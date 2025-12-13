package com.coc.modi.product.product.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
	
	Optional<Product> findById(Long id);
	
	Product saveAndFlush(Product product);
	
	void flush();
	
	List<Product> findByIdIn(List<Long> productIds);
}
