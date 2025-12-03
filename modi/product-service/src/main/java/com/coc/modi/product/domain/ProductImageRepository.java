package com.coc.modi.product.domain;

import java.util.List;

public interface ProductImageRepository {

    List<ProductImage> findByIdIn(List<Long> ids);
}
