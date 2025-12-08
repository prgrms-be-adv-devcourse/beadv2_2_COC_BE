package com.coc.modi.rental.rental.domain;

import java.time.LocalDate;
import java.util.List;

public interface RentalQueryRepository {

    List<Rental> search(LocalDate startDate, LocalDate endDate, RentalStatus rentalStatus);
}
