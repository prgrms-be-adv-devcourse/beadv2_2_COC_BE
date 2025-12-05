package com.coc.modi.rental.infrastructure;

import com.coc.modi.rental.domain.RentalEventLog;

public interface RentalEventLogRepository {

    void save(RentalEventLog eventLog);
}
