package com.coc.modi.product.viewlog.application;

import com.coc.modi.product.viewlog.domain.ProductViewDailyRepository;
import com.coc.modi.product.viewlog.domain.ProductViewLog;
import com.coc.modi.product.viewlog.domain.ProductViewLogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProductViewService {

	private final ProductViewLogRepository productViewLogRepository;
	private final ProductViewDailyRepository productViewDailyRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordView(Long productId, Long memberId) {
		if (productId == null) {
			return;
		}
		LocalDate viewDate = LocalDate.now();
		ProductViewLog log = ProductViewLog.create(productId, memberId, viewDate);
		productViewLogRepository.save(log);
		productViewDailyRepository.increment(viewDate, productId);
	}
}
