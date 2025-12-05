package com.coc.modi.rental.infrastructure;

import com.coc.modi.rental.domain.RentalItem;

import java.util.Optional;

public interface RentalItemRepository {

    Optional<RentalItem> findById(Long rentalItemId);
}
