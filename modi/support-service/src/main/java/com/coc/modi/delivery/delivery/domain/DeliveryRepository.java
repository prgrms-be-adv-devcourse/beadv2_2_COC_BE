package com.coc.modi.delivery.delivery.domain;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository {
	
	Delivery save(Delivery delivery);
	
	boolean existsByRentalItemIdAndCarrierCodeAndTrackingNumber(Long rentalItemId,
																String carrierCode,
																String trackingNumber);
	
	Optional<Delivery> findById(Long id);
	
	List<Delivery> findTargetsForTrackingWithSkipLocked(int intervalMinutes, int limit);
	
}
