package com.coc.modi.product.domain;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository {

    List<ProductImage> findByIdIn(List<Long> ids);

    Optional<ProductImage> findById(Long thumbnailId);
}
