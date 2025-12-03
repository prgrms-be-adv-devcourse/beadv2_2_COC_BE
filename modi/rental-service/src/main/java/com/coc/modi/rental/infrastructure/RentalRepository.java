package com.coc.modi.rental.infrastructure;

import com.coc.modi.rental.domain.Rental;

public interface RentalRepository {

    void save(Rental rental);
}
