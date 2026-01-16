package com.coc.modi.product.recommendation.infrastructure;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRecommendationRepository {
	
	private final JdbcTemplate jdbcTemplate;
	
	public List<ProductRecommendationRow> findRecommended(List<Double> vector, int size) {
		
		if (vector == null || vector.isEmpty() || size <= 0) {
			return List.of();
		}
		
		String vectorLiteral = vector.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(", ", "[", "]"));
		
		String sql = """
				SELECT p.id,
				       p.name,
				       p.category,
				       p.price_per_day,
				       pi.url AS thumbnail_url,
				       (p.embedding <-> ?::vector) AS distance
				FROM product p
				LEFT JOIN product_image pi ON pi.id = p.thumbnail_image_id
				WHERE p.status = 'ACTIVE'
				  AND p.embedding IS NOT NULL
				ORDER BY p.embedding <-> ?::vector
				LIMIT ?
				""";
		
		return jdbcTemplate.query(sql, new ProductRecommendationRowMapper(), vectorLiteral, vectorLiteral, size);
	}
	
	public record ProductRecommendationRow(
			Long productId,
			String name,
			String category,
			BigDecimal pricePerDay,
			String thumbnailUrl,
			Double distance
	) {
	}
	
	private static class ProductRecommendationRowMapper implements RowMapper<ProductRecommendationRow> {
		@Override
		public ProductRecommendationRow mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			return new ProductRecommendationRow(
					rs.getLong("id"),
					rs.getString("name"),
					rs.getString("category"),
					rs.getBigDecimal("price_per_day"),
					rs.getString("thumbnail_url"),
					rs.getDouble("distance")
			);
		}
	}
}
