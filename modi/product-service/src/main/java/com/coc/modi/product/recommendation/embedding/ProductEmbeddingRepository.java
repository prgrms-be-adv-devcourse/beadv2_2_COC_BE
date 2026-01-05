package com.coc.modi.product.recommendation.embedding;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductEmbeddingRepository {
	
	private final JdbcTemplate jdbcTemplate;
	
	public void updateEmbedding(Long productId, List<Double> vector) {
		
		if (productId == null || vector == null || vector.isEmpty()) {
			return;
		}
		
		String vectorLiteral = vector.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(", ", "[", "]"));
		
		jdbcTemplate.update(
				"UPDATE product SET embedding = ?::vector WHERE id = ?",
				vectorLiteral,
				productId
		);
	}
}
