package com.coc.modi.rental.rental.infrastructure;

import java.util.Optional;

import com.coc.modi.rental.rental.domain.Rental;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RentalJpaRepository extends JpaRepository<Rental, Long> {
	
	
	@Query("select r from Rental r left join fetch r.items where r.id = :rentalId")
	Optional<Rental> findByIdWithItems(Long rentalId);
}
