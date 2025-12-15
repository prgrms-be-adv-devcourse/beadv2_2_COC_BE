package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.infrastructure.client.rental.dto.RentalItemInfo;
import com.coc.modi.seller.exception.SettlementPeriodResolveException;

import org.springframework.batch.item.ItemProcessor;

import java.time.YearMonth;

public class SettlementAggregationProcessor implements ItemProcessor<RentalItemInfo, SettlementAggregationItem> {
	
	private final String requestedPeriodYm;
	
	public SettlementAggregationProcessor(String requestedPeriodYm) {
		
		this.requestedPeriodYm = requestedPeriodYm;
	}
	
	@Override
	public SettlementAggregationItem process(RentalItemInfo item) {
		
		if (item == null) {
			return null;
		}
		String resolvedPeriodYm = resolvePeriodYm(
				requestedPeriodYm,
				item.endDate()
		);
		return new SettlementAggregationItem(
				item.sellerId(),
				resolvedPeriodYm,
				item.rentalItemId(),
				item.memberId(),
				item.productId(),
				item.totalAmount()
		);
	}
	
	private String resolvePeriodYm(String periodYm,
								   java.time.LocalDate endDate) {
		
		// 1. 요청된 기간(Job Parameter)이 있으면 최우선으로 사용
		if (periodYm != null && !periodYm.isBlank()) {
			return periodYm;
		}
		
		// 2. end_date를 기준으로 월 결정 (없으면 실패시켜 데이터 수정 유도)
		if (endDate != null) {
			return YearMonth.from(endDate).toString();
		}
		
		throw new SettlementPeriodResolveException("“정산 기간을 결정하기 위한 데이터가 없습니다.”");
	}
}
