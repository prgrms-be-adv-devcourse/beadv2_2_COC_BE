package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductImageRepository;
import com.coc.modi.product.product.infrastructure.dto.ProductImageUrlDto;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryAdapter implements ProductImageRepository {
	
	private final ProductImageJpaRepository productImageJpaRepository;
	
	@Override
	public Optional<ProductImage> findById(Long thumbnailId) {
		
		return productImageJpaRepository.findById(thumbnailId);
	}
	
	@Override
	public Map<Long, String> findUrlMapByIds(List<Long> imageIds) {
		
		List<Long> ids = imageIds.stream()
				.filter(Objects::nonNull)
				.distinct().toList();
		
		if(ids.isEmpty()) {
			return Collections.emptyMap();
		}
		
		return productImageJpaRepository.findUrlDtoByIds(ids).stream()
				.collect(Collectors.toMap(
						ProductImageUrlDto::id,
						ProductImageUrlDto::url
				));
	}
}
