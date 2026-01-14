package com.coc.modi.product.searchlog.application;

import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.search.domain.ProductSortType;
import com.coc.modi.product.searchlog.domain.ProductSearchLog;
import com.coc.modi.product.searchlog.domain.ProductSearchLogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductSearchLogService {

	private final ProductSearchLogRepository productSearchLogRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordSearchLog(ProductSearchCondition condition,
								ProductSortType sortType,
								String cursor,
								int size,
								Long memberId) {

		String keyword = normalizeKeyword(condition.keyword());
		if (keyword == null) {
			return;
		}

		ProductSearchLog logEntity = ProductSearchLog.create(
				memberId,
				keyword,
				condition.category(),
				condition.minPrice(),
				condition.maxPrice(),
				condition.sellerId(),
				condition.startDate(),
				condition.endDate(),
				sortType,
				cursor,
				size
		);

		productSearchLogRepository.save(logEntity);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return null;
		}
		String trimmed = keyword.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return trimmed.toLowerCase(Locale.ROOT);
	}

	@Transactional(readOnly = true)
	public List<String> getRecentKeywords(Long memberId, int size) {
		if (memberId == null) {
			return List.of();
		}
		int resolvedSize = size > 0 ? size : 10;
		return productSearchLogRepository.findRecentKeywords(memberId, resolvedSize);
	}
}
