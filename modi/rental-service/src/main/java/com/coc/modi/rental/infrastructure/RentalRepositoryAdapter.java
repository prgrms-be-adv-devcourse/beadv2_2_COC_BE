package com.coc.modi.rental.infrastructure;

import com.coc.modi.rental.domain.Rental;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RentalRepositoryAdapter implements RentalRepository {

    private final RentalJpaRepository rentalJpaRepository;

    @Override
    public void save(Rental rental) {
        rentalJpaRepository.save(rental);
    }

}
