package com.coc.modi.seller.settlement.application.dto;

public record SettlementBatchRunCommand(
		String periodYm,
		String startDate,
		String endDate,
		Long sellerId,
		Integer pageSize
) {
}
