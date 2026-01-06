package com.coc.modi.product.search.application;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.product.product.application.dto.ProductListResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.application.dto.RentalResponse;
import com.coc.modi.product.product.infrastructure.client.RentalAvailabilityClient;
import com.coc.modi.product.product.presentation.dto.RentalRequest;
import com.coc.modi.product.product.exception.ProductSearchUnavailableException;
import com.coc.modi.product.search.infrastructure.ElasticsearchClientManager;
import com.coc.modi.product.search.infrastructure.ElasticsearchStatus;
import com.coc.modi.product.search.domain.ProductDocument;
import com.coc.modi.product.search.infrastructure.ProductSearchQueryRepository;
import com.coc.modi.product.search.domain.ProductSortType;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
		value = "product.search.elasticsearch.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class ElasticsearchProductSearchAdapter implements ProductSearchPort {
	
	private static final String INDEX = "products";
	
	private final ProductSearchQueryRepository searchQueryRepository;
	private final ElasticsearchClientManager clientManager;
	private final RentalAvailabilityClient rentalAvailabilityClient;
	private final ElasticsearchStatus status;
	
	@Override
	@Transactional(readOnly = true)
	public ProductScrollResponse searchProducts(ProductSearchCondition condition,
												String cursor,
												int size,
												ProductSortType sortType) {
		
		try {
			ProductSortType effectiveSortType = sortType != null ? sortType : ProductSortType.LATEST;
			
			List<ProductListResponse> items = new ArrayList<>();
			String currentCursor = cursor;
			boolean hasMoreFromEs = true;
			int safetyLimit = 5;
			
			List<ProductDocument> lastDocsBatch = List.of();
			
			while (items.size() < size && hasMoreFromEs && safetyLimit-- > 0) {
				
				// ES 1차 검색
				List<ProductDocument> docs = searchQueryRepository.search(condition, currentCursor, size, effectiveSortType);
				
				if (docs.isEmpty()) {
					hasMoreFromEs = false;
					break;
				}
				
				lastDocsBatch = docs;
				
				List<ProductDocument> availableDocs = docs;
				
				// 렌탈 기간 필터가 있는 경우
				if (condition.hasRentalPeriod()) {
					
					List<Long> productIds = docs.stream()
							.map(ProductDocument::getId)
							.toList();
					
					if (!productIds.isEmpty()) {
						
						RentalRequest request = new RentalRequest(condition.startDate(), condition.endDate(), productIds);
						
						RentalResponse rentalResponse = rentalAvailabilityClient.unavailableProducts(request);
						
						Set<Long> unavailableIds = new HashSet<>(
								Optional.ofNullable(rentalResponse)
										.map(RentalResponse::unavailableProductIds)
										.orElseGet(List::of)
						);
						
						availableDocs = docs.stream()
								.filter(doc -> !unavailableIds.contains(doc.getId()))
								.toList();
					}
				}
				
				items.addAll(availableDocs.stream()
						.map(ProductListResponse::from)
						.toList());
				
				currentCursor = buildNextCursor(docs, effectiveSortType);
				hasMoreFromEs = docs.size() == size && currentCursor != null;
			}
			
			if (items.isEmpty()) {
				status.markAvailable();
				return new ProductScrollResponse(List.of(), null, false);
			}
			
			if (items.size() > size) {
				items = items.subList(0, size);
			}
			
			String nextCursor = (hasMoreFromEs && !lastDocsBatch.isEmpty())
					? currentCursor
					: null;
			
			boolean hasNext = hasMoreFromEs;
			
			status.markAvailable();
			
			return new ProductScrollResponse(items, nextCursor, hasNext);
		} catch (Exception e) {
			
			status.markUnavailable(e);
			log.warn("Elasticsearch 검색 실패. 검색 기능 비활성화 처리. condition={}", condition, e);
			
			throw unavailable("상품 검색 기능을 일시적으로 사용할 수 없습니다.", e);
		}
	}
	
	@Override
	public void index(ProductDocument doc) {
		
		try {
			ElasticsearchClient client = clientManager.getClient();
			
			IndexRequest<ProductDocument> req = IndexRequest.of(b -> b
					.index(INDEX)
					.id(doc.getId().toString())
					.document(doc)
			);
			
			client.index(req);
			status.markAvailable();
		} catch (Exception e) {
			status.markUnavailable(e);
			log.warn("Elasticsearch 인덱싱 실패. productId={}", doc.getId(), e);
			throw unavailable("상품 검색 인덱싱 중 오류가 발생했습니다.", e);
		}
	}
	
	@Override
	public void deleteById(Long productId) {
		
		try {
			ElasticsearchClient client = clientManager.getClient();
			
			DeleteRequest req = DeleteRequest.of(b -> b.index(INDEX).id(productId.toString()));
			
			client.delete(req);
			status.markAvailable();
		} catch (Exception e) {
			status.markUnavailable(e);
			log.warn("Elasticsearch 삭제 실패. productId={}", productId, e);
			throw unavailable("상품 검색 인덱스 삭제 중 오류가 발생했습니다.", e);
		}
	}
	
	private ProductSearchUnavailableException unavailable(String message, Exception cause) {
		
		return new ProductSearchUnavailableException(message, cause);
	}
	
	private String buildNextCursor(List<ProductDocument> docs, ProductSortType sortType) {
		
		return switch (sortType) {
			case LATEST, OLDEST -> {
				// 뒤에서부터 createdAt 이 있는 문서를 찾음
				ProductDocument target = null;
				for (int i = docs.size() - 1; i >= 0; i--) {
					if (docs.get(i).getCreatedAt() != null) {
						target = docs.get(i);
						break;
					}
				}
				
				if (target == null) {
					// createdAt 이 하나도 없는 배치면 커서를 만들 수 없음
					yield null;
				}
				
				String rawCursor = target.getCreatedAt().toString() + "|" + target.getId();
				
				yield Base64.getUrlEncoder()
						.encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
			}
			case PRICE_HIGH, PRICE_LOW -> {
				ProductDocument target = null;
				for (int i = docs.size() - 1; i >= 0; i--) {
					if (docs.get(i).getPricePerDay() != null) {
						target = docs.get(i);
						break;
					}
				}
				
				if (target == null) {
					yield null;
				}
				
				BigDecimal price = target.getPricePerDay();
				Long id = target.getId();
				
				yield price.toPlainString() + ":" + id;
			}
		};
	}
}
