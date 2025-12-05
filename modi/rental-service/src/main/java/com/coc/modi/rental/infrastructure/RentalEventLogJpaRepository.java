package com.coc.modi.rental.infrastructure;

import com.coc.modi.rental.domain.RentalEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalEventLogJpaRepository extends JpaRepository<RentalEventLog, Long> {
}
