package com.coc.modi.product.product.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductImageRepository {
	
	Optional<ProductImage> findById(Long thumbnailId);
	
	Map<Long, String> findUrlMapByIds(List<Long> imageIds);
}
