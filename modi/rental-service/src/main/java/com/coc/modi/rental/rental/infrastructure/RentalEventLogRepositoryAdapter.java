package com.coc.modi.rental.rental.infrastructure;

import com.coc.modi.rental.rental.domain.RentalEventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RentalEventLogRepositoryAdapter implements RentalEventLogRepository {

    private final RentalEventLogJpaRepository rentalEventLogJpaRepository;

    @Override
    public void save(RentalEventLog eventLog) {

        rentalEventLogJpaRepository.save(eventLog);
    }
}
