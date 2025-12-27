package com.coc.modi.delivery.delivery.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.delivery.delivery.application.DeliveryService;
import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateCommand;
import com.coc.modi.delivery.delivery.application.dto.DeliveryCreateResponse;
import com.coc.modi.delivery.delivery.application.dto.DeliveryDetailResponse;
import com.coc.modi.delivery.delivery.presentation.dto.DeliveryCreateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliveries")
public class DeliveryController {
	
	private final DeliveryService deliveryService;
	
	// 배송 등록
	@PostMapping
	public ResponseEntity<ApiResponse<DeliveryCreateResponse>> createDelivery(@Valid @RequestBody DeliveryCreateRequest request) {
		
		DeliveryCreateCommand command = DeliveryCreateCommand.toCommand(request);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(deliveryService.createDelivery(command)));
	}
	
	// 배송 단건 조회
	@GetMapping("/{deliveryId}")
	public ResponseEntity<ApiResponse<DeliveryDetailResponse>> getDelivery(@PathVariable("deliveryId") Long deliveryId) {
		
		return ResponseEntity.ok(ApiResponse.ok(deliveryService.getDelivery(deliveryId)));
	}
}
