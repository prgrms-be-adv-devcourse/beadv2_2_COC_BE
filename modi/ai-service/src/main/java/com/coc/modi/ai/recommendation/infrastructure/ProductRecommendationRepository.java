package com.coc.modi.ai.recommendation.infrastructure;

import java.util.Collections;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRecommendationRepository {
	
	private final JdbcTemplate jdbcTemplate;
	private final ObjectMapper objectMapper;
	
	public List<ProductRecommendationRow> findRecommendedByVector(List<Double> vector,
																  List<String> categories,
																  int size) {
		
		if (vector == null || vector.isEmpty() || size <= 0) {
			return List.of();
		}
		
		String vectorLiteral = vector.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(", ", "[", "]"));
		List<Object> params = new ArrayList<>();
		params.add(vectorLiteral);
		
		String sql = """
				SELECT pe.product_id,
				       pe.name,
				       pe.category,
				       pe.specs,
				       pe.status,
				       (pe.embedding <-> ?::vector) AS distance
				FROM ai.product_embedding pe
				WHERE pe.status = 'ACTIVE'
				  AND pe.embedding IS NOT NULL
				""";
		
		sql = appendCategoryFilter(sql, categories, params);
		sql += """
				ORDER BY pe.embedding <-> ?::vector
				LIMIT ?
				""";
		params.add(vectorLiteral);
		params.add(size);
		
		return jdbcTemplate.query(sql, new ProductRecommendationRowMapper(objectMapper), params.toArray());
	}

	public List<ProductRecommendationRow> findRecommendedByProductId(Long productId,
																	 List<String> categories,
																	 int size) {
		
		if (productId == null || productId <= 0 || size <= 0) {
			return List.of();
		}
		List<Object> params = new ArrayList<>();
		params.add(productId);
		params.add(productId);
		
		String sql = """
				WITH target AS (
				    SELECT embedding
				    FROM ai.product_embedding
				    WHERE product_id = ?
				)
				SELECT pe.product_id,
				       pe.name,
				       pe.category,
				       pe.specs,
				       pe.status,
				       (pe.embedding <-> target.embedding) AS distance
				FROM ai.product_embedding pe
				CROSS JOIN target
				WHERE target.embedding IS NOT NULL
				  AND pe.product_id <> ?
				  AND pe.status = 'ACTIVE'
				  AND pe.embedding IS NOT NULL
				""";
		
		sql = appendCategoryFilter(sql, categories, params);
		sql += """
				ORDER BY pe.embedding <-> target.embedding
				LIMIT ?
				""";
		params.add(size);
		
		return jdbcTemplate.query(sql, new ProductRecommendationRowMapper(objectMapper), params.toArray());
	}

	public List<ProductRecommendationRow> findRecommendedByProductIds(List<Long> productIds,
																	  List<String> categories,
																	  int size) {
		
		if (productIds == null || productIds.isEmpty() || size <= 0) {
			return List.of();
		}
		
		List<Long> filteredIds = productIds.stream()
				.filter(id -> id != null && id > 0)
				.distinct()
				.toList();
		
		if (filteredIds.isEmpty()) {
			return List.of();
		}
		
		String placeholders = filteredIds.stream()
				.map(id -> "?")
				.collect(Collectors.joining(", "));
		
		List<Object> params = new ArrayList<>();
		params.addAll(filteredIds);
		params.addAll(filteredIds);
		
		String sql = """
				WITH target AS (
				    SELECT embedding
				    FROM ai.product_embedding
				    WHERE product_id IN (%s)
				)
				SELECT pe.product_id,
				       pe.name,
				       pe.category,
				       pe.specs,
				       pe.status,
				       MIN(pe.embedding <-> target.embedding) AS distance
				FROM ai.product_embedding pe
				CROSS JOIN target
				WHERE target.embedding IS NOT NULL
				  AND pe.product_id NOT IN (%s)
				  AND pe.status = 'ACTIVE'
				  AND pe.embedding IS NOT NULL
				""".formatted(placeholders, placeholders);
		
		sql = appendCategoryFilter(sql, categories, params);
		sql += """
				GROUP BY pe.product_id, pe.name, pe.category, pe.specs, pe.status
				ORDER BY distance
				LIMIT ?
				""";
		params.add(size);
		
		return jdbcTemplate.query(sql, new ProductRecommendationRowMapper(objectMapper), params.toArray());
	}

	private String appendCategoryFilter(String sql, List<String> categories, List<Object> params) {
		if (categories == null || categories.isEmpty()) {
			return sql;
		}
		List<String> filtered = categories.stream()
				.filter(cat -> cat != null && !cat.isBlank())
				.toList();
		if (filtered.isEmpty()) {
			return sql;
		}
		String placeholders = filtered.stream().map(c -> "?").collect(Collectors.joining(", "));
		params.addAll(filtered);
		return sql + " AND pe.category IN (" + placeholders + ") ";
	}
	
	public record ProductRecommendationRow(
			Long productId,
			String name,
			String category,
			Map<String, String> specs,
			String status,
			Double distance
	) {
	}
	
	private static class ProductRecommendationRowMapper implements RowMapper<ProductRecommendationRow> {
		private static final TypeReference<Map<String, String>> SPECS_TYPE = new TypeReference<>() {};
		private final ObjectMapper objectMapper;

		private ProductRecommendationRowMapper(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		@Override
		public ProductRecommendationRow mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			Map<String, String> specs = parseSpecs(rs.getString("specs"));
			return new ProductRecommendationRow(
					rs.getLong("product_id"),
					rs.getString("name"),
					rs.getString("category"),
					specs,
					rs.getString("status"),
					rs.getDouble("distance")
			);
		}

		private Map<String, String> parseSpecs(String raw) {
			if (raw == null || raw.isBlank()) {
				return Collections.emptyMap();
			}
			try {
				return objectMapper.readValue(raw, SPECS_TYPE);
			} catch (Exception ex) {
				return Collections.emptyMap();
			}
		}
	}
}
