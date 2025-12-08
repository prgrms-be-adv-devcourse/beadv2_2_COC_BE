package com.coc.modi.rental.rental.infrastructure;

import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RentalRepositoryAdapter implements RentalRepository {

    private final RentalJpaRepository rentalJpaRepository;

    @Override
    public void save(Rental rental) {

        rentalJpaRepository.save(rental);
    }

    @Override
    public Optional<Rental> findById(Long rentalId) {

        return rentalJpaRepository.findById(rentalId);
    }

}
