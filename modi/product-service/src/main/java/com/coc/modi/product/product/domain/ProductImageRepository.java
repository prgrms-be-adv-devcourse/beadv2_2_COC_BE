package com.coc.modi.product.product.domain;

import java.util.Optional;

public interface ProductImageRepository {
	
	Optional<ProductImage> findById(Long thumbnailId);
}
