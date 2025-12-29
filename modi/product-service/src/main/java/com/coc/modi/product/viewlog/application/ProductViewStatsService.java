package com.coc.modi.product.viewlog.application;

import com.coc.modi.product.searchlog.presentation.dto.PopularProductResponse;
import com.coc.modi.product.viewlog.domain.ProductViewDailyRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductViewStatsService {

	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 100;

	private final ProductViewDailyRepository productViewDailyRepository;

	@Transactional(readOnly = true)
	public List<PopularProductResponse> getPopularProducts(Integer size,
			LocalDate startDate,
			LocalDate endDate) {

		int limit = normalizeSize(size);
		List<Object[]> rows = productViewDailyRepository.findPopularProducts(startDate, endDate, limit);
		List<PopularProductResponse> results = new ArrayList<>(rows.size());
		for (Object[] row : rows) {
			if (row == null || row.length < 2) {
				continue;
			}
			Long productId = row[0] == null ? null : ((Number) row[0]).longValue();
			Long count = row[1] == null ? 0L : ((Number) row[1]).longValue();
			if (productId != null) {
				results.add(new PopularProductResponse(productId, count));
			}
		}
		return results;
	}

	private int normalizeSize(Integer size) {
		if (size == null || size <= 0) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}
}
