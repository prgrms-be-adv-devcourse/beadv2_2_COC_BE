package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductRepository;
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
	public Optional<Product> findByIdAndStatusNot(Long id,  ProductStatus status) {
		
		return productJpaRepository.findByIdAndStatusNot(id, status);
	}
	
	@Override
	public Optional<Product> findById(Long id) {
		
		return productJpaRepository.findById(id);
	}
	
	@Override
	public Page<Product> findBySellerIdAndStatusNot(Long sellerId, ProductStatus status, Pageable pageable) {
		
		return productJpaRepository.findBySellerIdAndStatusNot(sellerId, status, pageable);
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
}
