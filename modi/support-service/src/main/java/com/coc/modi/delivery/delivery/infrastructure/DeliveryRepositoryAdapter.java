package com.coc.modi.delivery.delivery.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.coc.modi.delivery.delivery.domain.Delivery;
import com.coc.modi.delivery.delivery.domain.DeliveryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryAdapter implements DeliveryRepository {
	
	private final DeliveryJpaRepository deliveryJpaRepository;
	
	@Override
	public Delivery save(Delivery delivery) {
		
		return deliveryJpaRepository.save(delivery);
	}
	
	@Override
	public java.util.Optional<Delivery> findById(Long id) {
		
		return deliveryJpaRepository.findById(id);
	}

	@Override
	public Optional<Delivery> findByRentalItemId(Long rentalItemId) {
		
		return deliveryJpaRepository.findByRentalItemId(rentalItemId);
	}
	
	@Override
	public List<Delivery> findTargetsForTrackingWithSkipLocked(int intervalMinutes, int limit) {
		
		return deliveryJpaRepository.findTargetsForTrackingWithSkipLocked(intervalMinutes, limit);
	}
}
