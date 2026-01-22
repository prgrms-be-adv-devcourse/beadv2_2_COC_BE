package com.coc.modi.product.viewlog.application;

import com.coc.modi.product.searchlog.presentation.dto.PopularProductResponse;
import com.coc.modi.product.support.StatsSizeNormalizer;
import com.coc.modi.product.viewlog.domain.ProductViewDailyRepository;
import com.coc.modi.product.viewlog.domain.PopularProductRow;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductViewStatsService {

	private final ProductViewDailyRepository productViewDailyRepository;

	@Transactional(readOnly = true)
	public List<PopularProductResponse> getPopularProducts(Integer size,
			LocalDate startDate,
			LocalDate endDate) {

		int limit = StatsSizeNormalizer.normalize(size);
		if (startDate == null && endDate == null) {
			LocalDate today = LocalDate.now();
			startDate = today;
			endDate = today;
		}
		List<PopularProductRow> rows = productViewDailyRepository.findPopularProducts(startDate, endDate, limit);
		List<PopularProductResponse> results = new ArrayList<>(rows.size());
		for (PopularProductRow row : rows) {
			if (row == null) {
				continue;
			}
			Long productId = row.getProductId();
			String productName = row.getProductName();
			Long count = row.getViewCount() == null ? 0L : row.getViewCount();
			if (productId != null) {
				results.add(new PopularProductResponse(productId, productName, count));
			}
		}
		return results;
	}

}
