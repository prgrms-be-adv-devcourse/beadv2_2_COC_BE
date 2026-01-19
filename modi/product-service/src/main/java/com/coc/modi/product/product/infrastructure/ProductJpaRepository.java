package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductModerationStatus;
import com.coc.modi.product.product.domain.ProductStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
	
	Page<Product> findBySellerIdAndStatusNot(Long sellerId, ProductStatus status, Pageable pageable);
	
	Optional<Product> findByIdAndStatusNot(Long id, ProductStatus status);
	
	List<Product> findByIdIn(Collection<Long> ids);

	List<Product> findByStatusNotAndModerationStatus(ProductStatus status, ProductModerationStatus moderationStatus);
}
