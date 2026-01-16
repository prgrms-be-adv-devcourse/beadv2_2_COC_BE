package com.coc.modi.delivery.delivery.domain;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository {
	
	Delivery save(Delivery delivery);
	
	Optional<Delivery> findById(Long id);

	Optional<Delivery> findByRentalItemId(Long rentalItemId);
	
	List<Delivery> findTargetsForTrackingWithSkipLocked(int intervalMinutes, int limit);
	
}
