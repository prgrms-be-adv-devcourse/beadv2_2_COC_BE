package com.coc.modi.rental.infrastructure;

import com.coc.modi.rental.domain.RentalItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RentalItemRepositoryAdapter implements RentalItemRepository {

    private final RentalItemJpaRepository rentalItemJpaRepository;

    @Override
    public Optional<RentalItem> findById(Long rentalItemId) {

        return rentalItemJpaRepository.findById(rentalItemId);
    }
}
