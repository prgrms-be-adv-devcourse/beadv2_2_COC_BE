package com.coc.modi.seller.settlement.presentation.dto;

import com.coc.modi.seller.settlement.application.dto.SettlementBatchRunCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SettlementBatchRunRequest(
        @NotBlank
        String periodYm,
        @NotBlank
        String startDate,
        @NotBlank
        String endDate,
        Long sellerId,
        @Positive
        Integer pageSize
) {
    public SettlementBatchRunCommand toCommand() {

        return new SettlementBatchRunCommand(periodYm, startDate, endDate, sellerId, pageSize);
    }
}
