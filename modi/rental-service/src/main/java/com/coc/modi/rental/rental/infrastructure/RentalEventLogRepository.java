package com.coc.modi.rental.rental.infrastructure;

import com.coc.modi.rental.rental.domain.RentalEventLog;

public interface RentalEventLogRepository {

    void save(RentalEventLog eventLog);
}
