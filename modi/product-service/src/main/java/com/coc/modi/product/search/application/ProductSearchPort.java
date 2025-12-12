package com.coc.modi.product.search.application;

import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.search.domain.ProductDocument;
import com.coc.modi.product.search.domain.ProductSortType;

public interface ProductSearchPort {
	
	ProductScrollResponse searchProducts(ProductSearchCondition condition,
										 String cursor,
										 int size,
										 ProductSortType sortType);
	
	void index(ProductDocument doc);
	
	void deleteById(Long productId);
}
