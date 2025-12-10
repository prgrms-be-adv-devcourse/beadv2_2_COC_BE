package com.coc.modi.rental.rental.domain;

import java.util.Optional;

public interface RentalRepository {
	
	void save(Rental rental);
	
	Optional<Rental> findById(Long rentalId);
}
