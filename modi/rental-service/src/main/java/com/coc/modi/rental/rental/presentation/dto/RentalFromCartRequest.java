package com.coc.modi.rental.rental.presentation.dto;

import com.coc.modi.rental.rental.application.dto.CreateRentalFromCartCommand;

import java.util.List;

public record RentalFromCartRequest(
        List<Long> cartItemIds
) {

    public CreateRentalFromCartCommand toCommand(Long memberId) {

        return new CreateRentalFromCartCommand(memberId, cartItemIds);
    }
}
