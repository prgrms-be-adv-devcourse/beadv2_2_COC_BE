package com.coc.modi.product.search.infrastructure;

import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.product.domain.ProductStatus;
import com.coc.modi.product.search.domain.ProductDocument;
import com.coc.modi.product.search.domain.ProductSortType;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductSearchQueryRepository {
	
	private final ElasticsearchOperations operations;
	
	public List<ProductDocument> search(
			ProductSearchCondition cond,
			String cursor,
			int size,
			ProductSortType sortType) {
		
		Pageable pageable = PageRequest.of(0, size);
		
		NativeQueryBuilder builder = NativeQuery.builder();
		builder.withQuery(q -> q
				.bool(b -> {
					// status ACTIVE 필터
					b.filter(f -> f.term(t -> t.field("status").value(ProductStatus.ACTIVE.name())));
					
					// 키워드 필터(name, description)
					if (StringUtils.hasText(cond.keyword())) {
						b.must(m -> m
								.multiMatch(mm -> mm.fields("name", "description").query(cond.keyword())));
					}
					
					// 카테고리 필터
					if (cond.category() != null) {
						ProductCategory category = cond.category();
						b.filter(f -> f.term(t -> t.field("category").value(category.name())));
					}
					
					// 가격 범위 필터
					if (cond.minPrice() != null || cond.maxPrice() != null) {
						b.filter(f -> f.range(r -> r.number(n -> {
							n.field("pricePerDay");
							
							if (cond.minPrice() != null) {
								n.gte(cond.minPrice().doubleValue());
							}
							if (cond.maxPrice() != null) {
								n.lte(cond.maxPrice().doubleValue());
							}
							
							return n;
						})));
					}
					
					// 판매자 필터
					if (cond.sellerId() != null) {
						b.filter(f -> f
								.term(t -> t.field("sellerId").value(cond.sellerId())));
					}
					
					if (StringUtils.hasText(cursor)) {
						switch (sortType) {
							case PRICE_HIGH -> {
								try {
									String[] parts = cursor.split(":", 2);
									BigDecimal cursorPrice = new BigDecimal(parts[0]);
									long cursorId = Long.parseLong(parts[1]);
									
									b.filter(f -> f.bool(bb -> bb
											// pricePerDay < cursorPrice
											.should(s -> s.range(r -> r.number(n -> n
													.field("pricePerDay")
													.lt(cursorPrice.doubleValue())
											)))
											// pricePerDay == cursorPrice AND id < cursorId
											.should(s -> s.bool(bb2 -> bb2
													.must(m -> m.term(t -> t
															.field("pricePerDay")
															.value(cursorPrice.doubleValue())
													))
													.must(m -> m.range(r -> r.number(n -> n
															.field("id")
															.lt((double)cursorId)
													)))
											))
											.minimumShouldMatch("1")
									));
								} catch (NumberFormatException ignored) {
									// cursor 파싱 실패하면 그냥 첫 페이지처럼 동작
								}
							}
							case PRICE_LOW -> {
								try {
									String[] parts = cursor.split(":", 2);
									BigDecimal cursorPrice = new BigDecimal(parts[0]);
									long cursorId = Long.parseLong(parts[1]);
									
									b.filter(f -> f.bool(bb -> bb
											// pricePerDay > cursorPrice
											.should(s -> s.range(r -> r.number(n -> n
													.field("pricePerDay")
													.gt(cursorPrice.doubleValue())
											)))
											// pricePerDay == cursorPrice AND id > cursorId
											.should(s -> s.bool(bb2 -> bb2
													.must(m -> m.term(t -> t
															.field("pricePerDay")
															.value(cursorPrice.doubleValue())
													))
													.must(m -> m.range(r -> r.number(n -> n
															.field("id")
															.gt((double)cursorId)
													)))
											))
											.minimumShouldMatch("1")
									));
								} catch (Exception ignored) {
									// 잘못된 cursor 형식이면 무시하고 첫 페이지처럼
								}
							}
							
						}
					}
					
					return b;
				})
		);
		
		switch (sortType) {
			case LATEST -> {
				builder.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
				builder.withSort(s -> s.field(f -> f.field("id").order(SortOrder.Desc)));
				
				applySearchAfterForCreatedAtAndId(builder, cursor);
			}
			case OLDEST -> {
				builder.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Asc)));
				builder.withSort(s -> s.field(f -> f.field("id").order(SortOrder.Asc)));
				
				applySearchAfterForCreatedAtAndId(builder, cursor);
			}
			case PRICE_HIGH -> {
				builder.withSort(s -> s.field(f -> f.field("pricePerDay").order(SortOrder.Desc)));
				builder.withSort(s -> s.field(f -> f.field("id").order(SortOrder.Desc)));
			}
			case PRICE_LOW -> {
				builder.withSort(s -> s.field(f -> f.field("pricePerDay").order(SortOrder.Asc)));
				builder.withSort(s -> s.field(f -> f.field("id").order(SortOrder.Asc)));
			}
		}
		
		NativeQuery query = builder.withPageable(pageable).build();
		
		SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);
		
		return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
	}
	
	private void applySearchAfterForCreatedAtAndId(NativeQueryBuilder builder, String cursor) {
		if (!StringUtils.hasText(cursor)) return;
		
		try {
			String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
			String[] parts = decoded.split(":", 2);
			
			long cursorMillis = Long.parseLong(parts[0]);
			long cursorId = Long.parseLong(parts[1]);
			
			// ✅ sort 순서와 동일하게 search_after도 넣는다 (createdAt, id)
			builder.withSearchAfter(List.of(
					JsonData.of(cursorMillis),
					JsonData.of(cursorId)
			));
		} catch (Exception ignored) {
			// 커서가 잘못되면 첫 페이지처럼 동작
		}
	}
}
