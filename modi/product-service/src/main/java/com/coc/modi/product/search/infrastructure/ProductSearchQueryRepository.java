package com.coc.modi.product.search.infrastructure;

import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.search.domain.ProductDocument;
import com.coc.modi.product.search.domain.ProductSortType;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
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
					
					// 키워드 필터(name, description)
					if (StringUtils.hasText(cond.keyword())) {
						b.must(m -> m
								.multiMatch(mm -> mm.fields("name", "description").query(cond.keyword())));
					}
					
					// 카테고리 필터
					if (cond.category() != null) {
						ProductCategory category = cond.category();
						b.filter(f -> f.term(t -> t.field("category.keyword").value(category.name())));
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
						applyCursorFilter(cursor, sortType, b);
					}
					
					return b;
				})
		);
		
		applySort(builder, sortType);
		
		NativeQuery query = builder.withPageable(pageable).build();
		
		SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);
		
		return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
	}

	private void applyCursorFilter(String cursor, ProductSortType sortType, BoolQuery.Builder builder) {
		switch (sortType) {
			case LATEST -> applyCreatedAtCursorFilter(builder, cursor, false);
			case OLDEST -> applyCreatedAtCursorFilter(builder, cursor, true);
			case PRICE_HIGH -> applyPriceCursorFilter(builder, cursor, false);
			case PRICE_LOW -> applyPriceCursorFilter(builder, cursor, true);
		}
	}

	private void applyCreatedAtCursorFilter(BoolQuery.Builder builder, String cursor, boolean isAsc) {
		try {
			CreatedAtCursor parsed = parseCreatedAtCursor(cursor);
			if (parsed == null) {
				return;
			}
			String cursorDate = parsed.date;
			Long cursorId = parsed.id;
			
			if (cursorId != null) {
				builder.filter(f -> f.bool(bb -> bb
						// createdAt < cursorDate OR createdAt > cursorDate
						.should(s -> s.range(r -> r.date(d -> {
							d.field("createdAt");
							if (isAsc) {
								d.gt(cursorDate);
							} else {
								d.lt(cursorDate);
							}
							return d;
						})))
						// createdAt == cursorDate AND id < cursorId OR id > cursorId
						.should(s -> s.bool(bb2 -> bb2
								.must(m -> m.term(t -> t
										.field("createdAt")
										.value(cursorDate)
								))
								.must(m -> m.range(r -> r.number(n -> {
									n.field("id");
									if (isAsc) {
										n.gt((double) cursorId);
									} else {
										n.lt((double) cursorId);
									}
									return n;
								})))
						))
						.minimumShouldMatch("1")
				));
			} else {
				builder.filter(f -> f.range(r -> r
						.date(d -> {
							d.field("createdAt");
							if (isAsc) {
								d.gt(cursorDate);
							} else {
								d.lt(cursorDate);
							}
							return d;
						})));
			}
		} catch (Exception ignored) {
			// 잘못된 cursor면 그냥 첫 페이지처럼 동작
		}
	}

	private void applyPriceCursorFilter(BoolQuery.Builder builder, String cursor, boolean isAsc) {
		try {
			PriceCursor parsed = parsePriceCursor(cursor);
			if (parsed == null) {
				return;
			}
			
			builder.filter(f -> f.bool(bb -> bb
					// pricePerDay < cursorPrice OR pricePerDay > cursorPrice
					.should(s -> s.range(r -> r.number(n -> {
						n.field("pricePerDay");
						if (isAsc) {
							n.gt(parsed.price.doubleValue());
						} else {
							n.lt(parsed.price.doubleValue());
						}
						return n;
					})))
					// pricePerDay == cursorPrice AND id < cursorId OR id > cursorId
					.should(s -> s.bool(bb2 -> bb2
							.must(m -> m.term(t -> t
									.field("pricePerDay")
									.value(parsed.price.doubleValue())
							))
							.must(m -> m.range(r -> r.number(n -> {
								n.field("id");
								if (isAsc) {
									n.gt((double) parsed.id);
								} else {
									n.lt((double) parsed.id);
								}
								return n;
							})))
					))
					.minimumShouldMatch("1")
			));
		} catch (Exception ignored) {
			// 잘못된 cursor 형식이면 무시하고 첫 페이지처럼
		}
	}

	private CreatedAtCursor parseCreatedAtCursor(String cursor) {
		String decoded = new String(
				Base64.getUrlDecoder().decode(cursor),
				StandardCharsets.UTF_8
		);
		String[] parts = decoded.split("\\|", 2);
		if (parts.length == 0 || parts[0].isBlank()) {
			return null;
		}
		Long id = null;
		if (parts.length == 2) {
			id = Long.parseLong(parts[1]);
		}
		return new CreatedAtCursor(parts[0], id);
	}

	private PriceCursor parsePriceCursor(String cursor) {
		String[] parts = cursor.split(":", 2);
		if (parts.length < 2) {
			return null;
		}
		BigDecimal price = new BigDecimal(parts[0]);
		Long id = Long.parseLong(parts[1]);
		return new PriceCursor(price, id);
	}

	private void applySort(NativeQueryBuilder builder, ProductSortType sortType) {
		switch (sortType) {
			case LATEST -> {
				builder.withSort(s -> s.field(f -> f
						.field("createdAt")
						.order(SortOrder.Desc)
				));
				builder.withSort(s -> s.field(f -> f
						.field("id")
						.order(SortOrder.Desc)
				));
			}
			case OLDEST -> {
				builder.withSort(s -> s.field(f -> f
						.field("createdAt")
						.order(SortOrder.Asc)
				));
				builder.withSort(s -> s.field(f -> f
						.field("id")
						.order(SortOrder.Asc)
				));
			}
			case PRICE_HIGH -> {
				// 가격 내림차순 + id 내림차순 (tie-breaker)
				builder.withSort(s -> s.field(f -> f
						.field("pricePerDay")
						.order(SortOrder.Desc)
				));
				builder.withSort(s -> s.field(f -> f
						.field("id")
						.order(SortOrder.Desc)
				));
			}
			case PRICE_LOW -> {
				// 가격 오름차순 + id 오름차순 (tie-breaker)
				builder.withSort(s -> s.field(f -> f
						.field("pricePerDay")
						.order(SortOrder.Asc)
				));
				builder.withSort(s -> s.field(f -> f
						.field("id")
						.order(SortOrder.Asc)
				));
			}
		}
	}

	private static final class CreatedAtCursor {
		private final String date;
		private final Long id;

		private CreatedAtCursor(String date, Long id) {
			this.date = date;
			this.id = id;
		}
	}

	private static final class PriceCursor {
		private final BigDecimal price;
		private final Long id;

		private PriceCursor(BigDecimal price, Long id) {
			this.price = price;
			this.id = id;
		}
	}
}
