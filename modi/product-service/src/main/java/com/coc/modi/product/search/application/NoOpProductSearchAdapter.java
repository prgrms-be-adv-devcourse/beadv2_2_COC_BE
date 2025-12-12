package com.coc.modi.product.search.application;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.search.infrastructure.ElasticsearchStatus;
import com.coc.modi.product.search.domain.ProductDocument;
import com.coc.modi.product.search.domain.ProductSortType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
		value = "product.search.elasticsearch.enabled",
		havingValue = "false"
)
public class NoOpProductSearchAdapter implements ProductSearchPort {
	
	private final ElasticsearchStatus elasticsearchStatus;
	
	@Override
	public ProductScrollResponse searchProducts(ProductSearchCondition condition,
												String cursor,
												int size,
												ProductSortType sortType) {
		
		elasticsearchStatus.markUnavailable(null);
		return new ProductScrollResponse(List.of(), null, false);
	}
	
	@Override
	public void index(ProductDocument doc) {
		
		elasticsearchStatus.markUnavailable(null);
	}
	
	@Override
	public void deleteById(Long productId) {
		
		elasticsearchStatus.markUnavailable(null);
	}
}
