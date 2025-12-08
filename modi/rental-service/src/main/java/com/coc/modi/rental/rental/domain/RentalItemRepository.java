package com.coc.modi.rental.rental.domain;

import java.util.Optional;

public interface RentalItemRepository {

    Optional<RentalItem> findById(Long rentalItemId);
}
