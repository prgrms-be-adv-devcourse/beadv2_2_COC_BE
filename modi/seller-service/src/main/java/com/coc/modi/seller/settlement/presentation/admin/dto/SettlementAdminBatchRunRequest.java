package com.coc.modi.seller.settlement.presentation.admin.dto;

public record SettlementAdminBatchRunRequest(
		String periodYm,
		String startDate,
		String endDate,
		Long sellerId,
		Integer pageSize
) {
}
