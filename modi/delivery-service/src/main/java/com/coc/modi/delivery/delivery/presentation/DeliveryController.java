package com.coc.modi.delivery.delivery.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.delivery.delivery.application.DeliveryService;
import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateCommand;
import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateResponse;
import com.coc.modi.delivery.delivery.application.dto.DeliveryDetailResponse;
import com.coc.modi.delivery.delivery.application.dto.DeliveryUpdateCommand;
import com.coc.modi.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.coc.modi.delivery.delivery.presentation.dto.DeliveryUpdateRequest;
import com.coc.modi.common.auth.CustomMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliveries")
public class DeliveryController {
	
	private final DeliveryService deliveryService;
	
	// 배송 등록
	@PostMapping
	public ResponseEntity<ApiResponse<DeliveryCreateResponse>> createDelivery(
			@AuthenticationPrincipal CustomMember member,
			@Valid @RequestBody DeliveryCreateRequest request) {
		
		DeliveryCreateCommand command = DeliveryCreateCommand.toCommand(request);
		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(deliveryService.createDelivery(command, member.memberId())));
	}
	
	// 배송 정보 수정
	@PatchMapping("/{rentalItemId}")
	public ResponseEntity<ApiResponse<DeliveryDetailResponse>> updateDeliveryByRentalItemId(
			@PathVariable("rentalItemId") Long rentalItemId,
			@AuthenticationPrincipal CustomMember member,
			@Valid @RequestBody DeliveryUpdateRequest request) {
		
		DeliveryUpdateCommand command = DeliveryUpdateCommand.toCommand(request);
		
		return ResponseEntity.ok(ApiResponse.ok(
			deliveryService.updateDeliveryByRentalItemId(rentalItemId, command, member.memberId())));
	}
	
	// 배송 단건 조회
	@GetMapping("/{deliveryId}")
	public ResponseEntity<ApiResponse<DeliveryDetailResponse>> getDelivery(
			@PathVariable("deliveryId") Long deliveryId,
			@org.springframework.security.core.annotation.AuthenticationPrincipal com.coc.modi.common.auth.CustomMember member) {
		
		return ResponseEntity.ok(ApiResponse.ok(deliveryService.getDelivery(deliveryId, member.memberId())));
	}

	// rentalItemId로 배송 단건 조회
	@GetMapping("/rental-items/{rentalItemId}")
	public ResponseEntity<ApiResponse<DeliveryDetailResponse>> getDeliveryByRentalItemId(
			@PathVariable("rentalItemId") Long rentalItemId,
			@org.springframework.security.core.annotation.AuthenticationPrincipal com.coc.modi.common.auth.CustomMember member) {
		
		return ResponseEntity.ok(ApiResponse.ok(
				deliveryService.getDeliveryByRentalItemId(rentalItemId, member.memberId())));
	}
}
