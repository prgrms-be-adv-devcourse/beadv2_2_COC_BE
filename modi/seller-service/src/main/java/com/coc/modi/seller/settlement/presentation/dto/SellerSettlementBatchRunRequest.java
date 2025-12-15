package com.coc.modi.seller.settlement.presentation.dto;

import com.coc.modi.seller.settlement.application.dto.SettlementBatchRunCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SellerSettlementBatchRunRequest(
		@NotBlank
		String periodYm,
		String startDate,
		String endDate,
		@Positive
		Integer pageSize
) {
	
	public SettlementBatchRunCommand toCommand(Long sellerId, String resolvedStartDate, String resolvedEndDate) {
		
		return new SettlementBatchRunCommand(
				periodYm,
				resolvedStartDate,
				resolvedEndDate,
				sellerId,
				pageSize
		);
	}
}
