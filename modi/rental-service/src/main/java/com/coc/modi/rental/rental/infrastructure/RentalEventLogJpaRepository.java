package com.coc.modi.rental.rental.infrastructure;

import com.coc.modi.rental.rental.domain.RentalEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalEventLogJpaRepository extends JpaRepository<RentalEventLog, Long> {
}
