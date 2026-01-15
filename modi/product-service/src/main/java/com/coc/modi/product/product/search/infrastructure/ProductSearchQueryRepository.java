package com.coc.modi.product.product.search.infrastructure;

import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductStatus;
import com.coc.modi.product.product.search.domain.ProductSortType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProductSearchQueryRepository {

	private static final ZoneOffset CURSOR_ZONE = ZoneOffset.ofHours(9);
	private final EntityManager entityManager;

	public List<Product> search(
			ProductSearchCondition cond,
			String normalizedKeyword,
			String cursor,
			int size,
			ProductSortType sortType) {

		QuerySpec spec = buildQuery(cond, normalizedKeyword, cursor, sortType);
		Query query = entityManager.createNativeQuery(spec.sql, Product.class);
		spec.params.forEach(query::setParameter);
		query.setMaxResults(size);
		return query.getResultList();
	}

	private QuerySpec buildQuery(ProductSearchCondition cond,
								 String normalizedKeyword,
								 String cursor,
								 ProductSortType sortType) {
		StringBuilder sql = new StringBuilder("SELECT p.* FROM product.product p");
		List<String> where = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		where.add("p.status <> :deletedStatus");
		params.put("deletedStatus", ProductStatus.DELETE.name());

		if (cond != null) {
			if (StringUtils.hasText(cond.keyword()) || StringUtils.hasText(normalizedKeyword)) {
				List<String> keywordFilters = new ArrayList<>();
				if (StringUtils.hasText(cond.keyword())) {
					keywordFilters.add("to_tsvector('simple', coalesce(p.name, '') || ' ' || coalesce(p.description, '')) " +
							"@@ plainto_tsquery('simple', :keywordRaw)");
					params.put("keywordRaw", cond.keyword());
				}
				if (StringUtils.hasText(normalizedKeyword)) {
					keywordFilters.add("to_tsvector('simple', " +
							"coalesce(product.normalize_keyword_search(p.name), '') || ' ' || " +
							"coalesce(product.normalize_keyword_search(p.description), '')) " +
							"@@ plainto_tsquery('simple', :keywordNorm)");
					params.put("keywordNorm", normalizedKeyword);
				}
				if (!keywordFilters.isEmpty()) {
					where.add("(" + String.join(" OR ", keywordFilters) + ")");
				}
			}
			if (cond.category() != null) {
				where.add("p.category = :category");
				params.put("category", cond.category().name());
			}
			if (cond.minPrice() != null) {
				where.add("p.price_per_day >= :minPrice");
				params.put("minPrice", cond.minPrice());
			}
			if (cond.maxPrice() != null) {
				where.add("p.price_per_day <= :maxPrice");
				params.put("maxPrice", cond.maxPrice());
			}
			if (cond.sellerId() != null) {
				where.add("p.seller_id = :sellerId");
				params.put("sellerId", cond.sellerId());
			}
		}

		if (StringUtils.hasText(cursor)) {
			applyCursorFilter(where, params, cursor, sortType);
		}

		if (!where.isEmpty()) {
			sql.append(" WHERE ").append(String.join(" AND ", where));
		}

		sql.append(" ORDER BY ").append(resolveSort(sortType));
		return new QuerySpec(sql.toString(), params);
	}

	private void applyCursorFilter(List<String> where, Map<String, Object> params, String cursor, ProductSortType sortType) {
		try {
			switch (sortType) {
				case LATEST -> applyCreatedAtCursorFilter(where, params, cursor, false);
				case OLDEST -> applyCreatedAtCursorFilter(where, params, cursor, true);
				case PRICE_HIGH -> applyPriceCursorFilter(where, params, cursor, false);
				case PRICE_LOW -> applyPriceCursorFilter(where, params, cursor, true);
			}
		} catch (Exception ignored) {
			// 잘못된 cursor면 첫 페이지처럼 동작
		}
	}

	private void applyCreatedAtCursorFilter(List<String> where, Map<String, Object> params, String cursor, boolean isAsc) {
		CreatedAtCursor parsed = parseCreatedAtCursor(cursor);
		if (parsed == null || parsed.date == null) {
			return;
		}

		if (parsed.id != null) {
			if (isAsc) {
				where.add("(date_trunc('milliseconds', p.created_at) > :cursorDate OR " +
						"(date_trunc('milliseconds', p.created_at) = :cursorDate AND p.id > :cursorId))");
			} else {
				where.add("(date_trunc('milliseconds', p.created_at) < :cursorDate OR " +
						"(date_trunc('milliseconds', p.created_at) = :cursorDate AND p.id < :cursorId))");
			}
			params.put("cursorId", parsed.id);
		} else {
			if (isAsc) {
				where.add("date_trunc('milliseconds', p.created_at) > :cursorDate");
			} else {
				where.add("date_trunc('milliseconds', p.created_at) < :cursorDate");
			}
		}

		params.put("cursorDate", parsed.date);
	}

	private void applyPriceCursorFilter(List<String> where, Map<String, Object> params, String cursor, boolean isAsc) {
		PriceCursor parsed = parsePriceCursor(cursor);
		if (parsed == null || parsed.price == null || parsed.id == null) {
			return;
		}

		if (isAsc) {
			where.add("(p.price_per_day > :cursorPrice OR (p.price_per_day = :cursorPrice AND p.id > :cursorId))");
		} else {
			where.add("(p.price_per_day < :cursorPrice OR (p.price_per_day = :cursorPrice AND p.id < :cursorId))");
		}
		params.put("cursorPrice", parsed.price);
		params.put("cursorId", parsed.id);
	}

	private CreatedAtCursor parseCreatedAtCursor(String cursor) {
		byte[] decodedBytes = Base64.getUrlDecoder().decode(cursor);
		String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
		String[] parts = decoded.split("\\|", 2);
		if (parts.length == 0 || parts[0].isBlank()) {
			return null;
		}
		Long id = null;
		if (parts.length == 2) {
			id = Long.parseLong(parts[1]);
		}
		long epochMillis = Long.parseLong(parts[0]);
		LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), CURSOR_ZONE);
		return new CreatedAtCursor(date, id);
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

	private String resolveSort(ProductSortType sortType) {
		return switch (sortType) {
			case LATEST -> "date_trunc('milliseconds', p.created_at) DESC, p.id DESC";
			case OLDEST -> "date_trunc('milliseconds', p.created_at) ASC, p.id ASC";
			case PRICE_HIGH -> "p.price_per_day DESC, p.id DESC";
			case PRICE_LOW -> "p.price_per_day ASC, p.id ASC";
		};
	}

	private record QuerySpec(String sql, Map<String, Object> params) {
	}

	private static final class CreatedAtCursor {
		private final LocalDateTime date;
		private final Long id;

		private CreatedAtCursor(LocalDateTime date, Long id) {
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
