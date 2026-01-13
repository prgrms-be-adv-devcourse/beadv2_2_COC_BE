package com.coc.modi.product.product.search.application;

import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.search.domain.ProductSortType;

public interface ProductSearchPort {
	
	ProductScrollResponse searchProducts(ProductSearchCondition condition,
										 String cursor,
										 int size,
										 ProductSortType sortType);
}
