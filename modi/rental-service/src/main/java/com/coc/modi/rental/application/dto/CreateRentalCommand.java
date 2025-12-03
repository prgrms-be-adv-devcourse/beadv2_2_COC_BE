package com.coc.modi.rental.application.dto;

import java.util.List;

public record CreateRentalCommand(
        Long memberId,
        List<RentalItemSpec> items
) {}
