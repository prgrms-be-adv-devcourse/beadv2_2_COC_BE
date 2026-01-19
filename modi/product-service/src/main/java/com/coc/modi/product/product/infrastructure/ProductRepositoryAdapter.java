package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.product.domain.ProductModerationStatus;
import com.coc.modi.product.product.domain.ProductStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {
	
	private final ProductJpaRepository productJpaRepository;
	
	@Override
	public Optional<Product> findNonDeletedById(Long id) {

		return productJpaRepository.findByIdAndStatusNot(id, ProductStatus.DELETE);
	}
	
	@Override
	public Optional<Product> findById(Long id) {
		
		return productJpaRepository.findById(id);
	}
	
	@Override
	public Page<Product> findNonDeletedBySellerId(Long sellerId, Pageable pageable) {

		return productJpaRepository.findBySellerIdAndStatusNot(sellerId, ProductStatus.DELETE, pageable);
	}

	@Override
	public Page<Product> findNonDeletedByModerationStatus(ProductModerationStatus moderationStatus,
														  Pageable pageable) {

		return productJpaRepository.findByStatusNotAndModerationStatus(
				ProductStatus.DELETE,
				moderationStatus,
				pageable
		);
	}
	
	@Override
	public Product saveAndFlush(Product product) {
		
		return productJpaRepository.saveAndFlush(product);
	}
	
	@Override
	public void flush() {
		
		productJpaRepository.flush();
	}
	
	@Override
	public List<Product> findByIdIn(List<Long> productIds) {
		
		return productJpaRepository.findByIdIn(productIds);
	}

	@Override
	public List<Product> findNonDeletedByModerationStatus(ProductModerationStatus moderationStatus) {

		return productJpaRepository.findByStatusNotAndModerationStatus(
				ProductStatus.DELETE,
				moderationStatus
		);
	}
}
