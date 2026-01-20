package com.coc.modi.ai.embedding.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.coc.modi.ai.embedding.domain.ProductEmbeddingTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductEmbeddingRepository {
	
	private final JdbcTemplate jdbcTemplate;
	private final ObjectMapper objectMapper;

	public List<Long> findProductIdsWithEmbedding() {
		
		String sql = "SELECT product_id FROM ai.product_embedding WHERE embedding IS NOT NULL";
		return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("product_id"));
	}
	
	public boolean hasEmbedding(Long productId) {
		
		if (productId == null) {
			return false;
		}
		
		String sql = "SELECT COUNT(1) FROM ai.product_embedding WHERE product_id = ? AND embedding IS NOT NULL";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, productId);
		return count != null && count > 0;
	}
	
	public void upsert(ProductEmbeddingTarget target, List<Double> vector) {
		
		if (target == null || target.productId() == null || vector == null || vector.isEmpty()) {
			return;
		}
		
		String vectorLiteral = vector.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(", ", "[", "]"));
		
		String specsJson = toSpecsJson(target.specs());
		
		String sql = """
				INSERT INTO ai.product_embedding
				    (product_id, name, description, category, specs, status, embedding)
				VALUES
				    (?, ?, ?, ?, ?::jsonb, ?, ?::vector)
				ON CONFLICT (product_id) DO UPDATE
				SET name = EXCLUDED.name,
				    description = EXCLUDED.description,
				    category = EXCLUDED.category,
				    specs = EXCLUDED.specs,
				    status = EXCLUDED.status,
				    embedding = EXCLUDED.embedding
				""";
		
		jdbcTemplate.update(sql,
				target.productId(),
				target.name(),
				target.description(),
				target.category(),
				specsJson,
				target.status(),
				vectorLiteral
		);
	}

	private String toSpecsJson(Map<String, String> specs) {
		if (specs == null || specs.isEmpty()) {
			return "{}";
		}
		try {
			return objectMapper.writeValueAsString(specs);
		} catch (JsonProcessingException ex) {
			return "{}";
		}
	}
}
