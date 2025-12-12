package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.infrastructure.client.rental.dto.RentalItemInfo;

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
				item.paidAt(),
				item.startDate(),
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
								   java.time.LocalDateTime paidAt,
								   java.time.LocalDate startDate,
								   java.time.LocalDate endDate) {
		
		if (periodYm != null && !periodYm.isBlank()) {
			return periodYm;
		}
		if (paidAt != null) {
			return YearMonth.from(paidAt.toLocalDate()).toString();
		}
		java.time.LocalDate fallback = startDate != null ? startDate : endDate;
		if (fallback != null) {
			return YearMonth.from(fallback).toString();
		}
		return null;
	}
}
