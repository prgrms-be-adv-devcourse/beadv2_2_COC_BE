package com.coc.modi.delivery.delivery.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateCommand;
import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateResponse;
import com.coc.modi.delivery.delivery.application.dto.DeliveryDetailResponse;
import com.coc.modi.delivery.delivery.application.dto.DeliveryUpdateCommand;
import com.coc.modi.delivery.delivery.domain.Delivery;
import com.coc.modi.delivery.delivery.domain.DeliveryRepository;
import com.coc.modi.delivery.delivery.exception.DeliveryConflictException;
import com.coc.modi.delivery.delivery.exception.DeliveryForbiddenException;
import com.coc.modi.delivery.delivery.exception.DeliveryNotFoundException;
import com.coc.modi.delivery.delivery.infrastructure.client.rental.RentalInternalFeignClient;
import com.coc.modi.delivery.delivery.infrastructure.client.rental.dto.RentalItemSellerResponse;
import com.coc.modi.delivery.delivery.infrastructure.client.seller.SellerInternalFeignClient;
import com.coc.modi.delivery.delivery.infrastructure.client.seller.dto.SellerIdResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {
	
	private final DeliveryRepository deliveryRepository;
	private final RentalInternalFeignClient rentalInternalFeignClient;
	private final SellerInternalFeignClient sellerInternalFeignClient;
	
	// 배송 등록
	@Transactional
	public DeliveryCreateResponse createDelivery(DeliveryCreateCommand command, Long memberId) {
		
		validateSeller(command.rentalItemId(), memberId);
		
		boolean exists = deliveryRepository.findByRentalItemId(command.rentalItemId())
				.isPresent();
		if (exists) {
			throw new DeliveryConflictException(
					"이미 등록된 배송 정보입니다. rentalItemId: " + command.rentalItemId());
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
	
	// 배송 정보 수정
	@Transactional
	public DeliveryDetailResponse updateDeliveryByRentalItemId(Long rentalItemId,
			DeliveryUpdateCommand command,
			Long memberId) {
		
		validateSeller(rentalItemId, memberId);
		
		Delivery delivery = deliveryRepository.findByRentalItemId(rentalItemId)
				.orElseThrow(() -> new DeliveryNotFoundException("rentalItemId", rentalItemId));
		
		delivery.updateTrackingInfo(command.carrierCode(), command.trackingNumber());
		
		return DeliveryDetailResponse.from(delivery);
	}
	
	// 배송 단건 조회
	@Transactional(readOnly = true)
	public DeliveryDetailResponse getDelivery(Long deliveryId, Long memberId) {
		
		Delivery delivery = deliveryRepository.findById(deliveryId)
				.orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
		
		validateAccess(delivery.getRentalItemId(), memberId);
		
		return DeliveryDetailResponse.from(delivery);
	}
	
	// rentalItemId로 배송 단건 조회
	@Transactional(readOnly = true)
	public DeliveryDetailResponse getDeliveryByRentalItemId(Long rentalItemId, Long memberId) {
		
		validateAccess(rentalItemId, memberId);
		
		Delivery delivery = deliveryRepository.findByRentalItemId(rentalItemId)
				.orElseThrow(() -> new DeliveryNotFoundException("rentalItemId", rentalItemId));
		
		return DeliveryDetailResponse.from(delivery);
	}

	private void validateSeller(Long rentalItemId, Long memberId) {
		
		if (memberId == null) {
			throw new DeliveryForbiddenException("판매자 정보가 없습니다.");
		}
		
		RentalItemSellerResponse rentalInfo = rentalInternalFeignClient.getRentalItemSeller(rentalItemId);
		SellerIdResponse sellerInfo = sellerInternalFeignClient.getSellerByMember(memberId);
		
		if (sellerInfo == null || sellerInfo.sellerId() == null) {
			throw new DeliveryForbiddenException("판매자 정보를 찾을 수 없습니다. memberId: " + memberId);
		}
		
		if (!sellerInfo.sellerId().equals(rentalInfo.sellerId())) {
			throw new DeliveryForbiddenException(
					"판매자 정보가 일치하지 않습니다. rentalItemId: " + rentalItemId
							+ ", memberId: " + memberId);
		}
	}
	
	private void validateAccess(Long rentalItemId, Long memberId) {
		
		if (memberId == null) {
			throw new DeliveryForbiddenException("회원 정보가 없습니다.");
		}
		
		RentalItemSellerResponse rentalInfo = rentalInternalFeignClient.getRentalItemSeller(rentalItemId);
		SellerIdResponse sellerInfo = sellerInternalFeignClient.getSellerByMember(memberId);
		
		Long sellerId = sellerInfo != null ? sellerInfo.sellerId() : null;
		Long buyerId = rentalInfo != null ? rentalInfo.memberId() : null;
		
		boolean sellerMatch = sellerId != null && rentalInfo != null && sellerId.equals(rentalInfo.sellerId());
		boolean buyerMatch = buyerId != null && buyerId.equals(memberId);
		
		if (!sellerMatch && !buyerMatch) {
			throw new DeliveryForbiddenException(
					"조회 권한이 없습니다. rentalItemId: " + rentalItemId + ", memberId: " + memberId);
		}
	}
}
