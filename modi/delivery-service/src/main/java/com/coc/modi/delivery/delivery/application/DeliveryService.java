package com.coc.modi.delivery.delivery.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateCommand;
import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateResponse;
import com.coc.modi.delivery.delivery.application.dto.DeliveryDetailResponse;
import com.coc.modi.delivery.delivery.domain.Delivery;
import com.coc.modi.delivery.delivery.domain.DeliveryRepository;
import com.coc.modi.delivery.delivery.exception.DeliveryConflictException;
import com.coc.modi.delivery.delivery.exception.DeliveryNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {
	
	private final DeliveryRepository deliveryRepository;
	
	// 배송 등록
	@Transactional
	public DeliveryCreateResponse createDelivery(DeliveryCreateCommand command) {
		
		boolean exists = deliveryRepository.existsByRentalItemIdAndCarrierCodeAndTrackingNumber(
				command.rentalItemId(), command.carrierCode(), command.trackingNumber());
		
		if (exists) {
			
			throw new DeliveryConflictException(
					"이미 등록된 배송 정보입니다. rentalItemId: " + command.rentalItemId()
							+ ", trackingNumber: " + command.trackingNumber());
		}
		
		Delivery delivery = Delivery.create(
				command.rentalItemId(),
				command.carrierCode(),
				command.trackingNumber()
		);
		
		Delivery saved = deliveryRepository.save(delivery);
		
		return new DeliveryCreateResponse(
				saved.getId(),
				saved.getRentalItemId(),
				saved.getCarrierCode(),
				saved.getTrackingNumber(),
				saved.getStatus());
	}
	
	// 배송 단건 조회
	@Transactional(readOnly = true)
	public DeliveryDetailResponse getDelivery(Long deliveryId) {
		
		Delivery delivery = deliveryRepository.findById(deliveryId)
				.orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
		
		return DeliveryDetailResponse.from(delivery);
	}
}
