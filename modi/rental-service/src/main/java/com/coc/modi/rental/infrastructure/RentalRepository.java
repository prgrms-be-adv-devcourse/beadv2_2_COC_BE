package com.coc.modi.rental.infrastructure;

import com.coc.modi.rental.domain.Rental;

import java.util.Optional;

public interface RentalRepository {

    void save(Rental rental);

    Optional<Rental> findById(Long rentalId);
}
