package com.coc.modi.rental.infrastructure;

import com.coc.modi.rental.domain.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalJpaRepository extends JpaRepository<Rental, Long> {
}
