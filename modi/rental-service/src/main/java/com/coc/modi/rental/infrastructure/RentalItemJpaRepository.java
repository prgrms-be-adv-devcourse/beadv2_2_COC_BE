package com.coc.modi.rental.infrastructure;

import com.coc.modi.rental.domain.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalItemJpaRepository extends JpaRepository<RentalItem, Long> {
}
