package com.coc.modi.seller.settlement.application.dto;

public record SettlementBulkPayResponse(
		int total,
		int requested,
		int skipped
) {
}
