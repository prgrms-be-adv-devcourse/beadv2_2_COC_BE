package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductImageRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryAdapter implements ProductImageRepository {
	
	private final ProductImageJpaRepository productImageJpaRepository;
	
	@Override
	public Optional<ProductImage> findById(Long thumbnailId) {
		
		return productImageJpaRepository.findById(thumbnailId);
	}
}
