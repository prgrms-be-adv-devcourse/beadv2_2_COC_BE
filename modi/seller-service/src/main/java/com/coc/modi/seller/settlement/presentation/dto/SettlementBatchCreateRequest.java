package com.coc.modi.seller.settlement.presentation.dto;

import com.coc.modi.seller.settlement.application.dto.SettlementBatchCreateCommand;

import jakarta.validation.constraints.NotBlank;

public record SettlementBatchCreateRequest(
        @NotBlank
        String periodYm
) {
    public SettlementBatchCreateCommand toCommand() {

        return new SettlementBatchCreateCommand(periodYm);
    }
}
