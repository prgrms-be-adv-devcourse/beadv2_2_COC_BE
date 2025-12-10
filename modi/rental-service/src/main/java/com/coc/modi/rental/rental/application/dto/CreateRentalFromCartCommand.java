package com.coc.modi.rental.rental.application.dto;

import java.util.List;

public record CreateRentalFromCartCommand(
		Long memberId,
		List<Long> cartItemIds
) {
}
