package com.coc.modi.rental.rental.domain;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalQueryRepository {
	
	List<Rental> search(LocalDate startDate, LocalDate endDate, RentalStatus rentalStatus);
	
	Page<RentalItem> findRentalItemsBySellerAndProduct(Long sellerId,
													   Long productId,
													   RentalItemStatus status,
													   LocalDate startDate,
													   LocalDate endDate,
													   Pageable pageable);
	
	Page<RentalItem> findCompletedRentalItemsBySeller(Long sellerId,
													  LocalDate startDate,
													  LocalDate endDate,
													  Pageable pageable);
	
	List<Long> findUnavailableProductIds(LocalDate startDate, LocalDate endDate, List<Long> productIds);
}
