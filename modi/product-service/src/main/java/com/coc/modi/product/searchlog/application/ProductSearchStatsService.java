package com.coc.modi.product.searchlog.application;

import com.coc.modi.product.searchlog.domain.ProductSearchLog;
import com.coc.modi.product.searchlog.domain.ProductSearchLogRepository;
import com.coc.modi.product.searchlog.presentation.dto.PopularKeywordResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductSearchStatsService {

	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 100;

	private final ProductSearchLogRepository productSearchLogRepository;

	@Transactional(readOnly = true)
	public List<PopularKeywordResponse> getPopularKeywords(Integer size,
										   LocalDate startDate,
										   LocalDate endDate) {

		int limit = normalizeSize(size);
		List<ProductSearchLog> logs = loadLogs(startDate, endDate);

		Map<String, Long> counts = new HashMap<>();
		for (ProductSearchLog logEntry : logs) {
			String keyword = logEntry.getKeyword();
			if (keyword == null || keyword.isBlank()) {
				continue;
			}
			counts.merge(keyword, 1L, Long::sum);
		}

		return counts.entrySet().stream()
				.sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed()
						.thenComparing(Map.Entry::getKey))
				.limit(limit)
				.map(entry -> new PopularKeywordResponse(entry.getKey(), entry.getValue()))
				.toList();
	}

	private int normalizeSize(Integer size) {
		if (size == null || size <= 0) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}

	private List<ProductSearchLog> loadLogs(LocalDate startDate, LocalDate endDate) {
		LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
		LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

		if (start == null && end == null) {
			return productSearchLogRepository.findAll();
		}
		if (start != null && end != null) {
			return productSearchLogRepository.findByCreatedAtBetween(start, end);
		}
		if (start != null) {
			return productSearchLogRepository.findByCreatedAtGreaterThanEqual(start);
		}
		return productSearchLogRepository.findByCreatedAtLessThanEqual(end);
	}
}
