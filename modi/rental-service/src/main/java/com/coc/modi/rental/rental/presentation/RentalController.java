package com.coc.modi.rental.rental.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.application.*;
import com.coc.modi.rental.rental.application.dto.PayRentalResponse;
import com.coc.modi.rental.rental.application.dto.RentalResponse;
import com.coc.modi.rental.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.presentation.dto.*;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rentals")
@Validated
public class RentalController {
	
	private final RentalCreationService rentalCreationService;
	private final RentalDecisionService rentalDecisionService;
	private final RentalLifecycleService rentalLifecycleService;
	private final RentalPaymentService rentalPaymentService;
	private final RentalQueryService rentalQueryService;
	
	@PostMapping("/carts")
	public ResponseEntity<ApiResponse<Void>> createRentalFromCart(@Valid @RequestBody RentalFromCartRequest rentalFromCartRequest,
																  Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		rentalCreationService.createRentalFromCart(rentalFromCartRequest.toCommand(memberId));
		
		return ResponseEntity.status(201).body(ApiResponse.ok(null));
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createRental(@Valid @RequestBody RentalRequest rentalRequest,
														  Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		rentalCreationService.createRental(rentalRequest.toCommand(memberId));
		
		return ResponseEntity.status(201).body(ApiResponse.ok(null));
	}
	
	@PatchMapping("/{rentalItemId}/accept")
	public ResponseEntity<ApiResponse<Void>> acceptRentalItem(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
															  Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		rentalDecisionService.acceptRentalItem(rentalItemId, memberId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@PatchMapping("/{rentalItemId}/reject")
	public ResponseEntity<ApiResponse<Void>> rejectRentalItem(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
															  Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		rentalDecisionService.rejectRentalItem(rentalItemId, memberId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@PostMapping("/{rentalId}/pay")
	public ResponseEntity<ApiResponse<PayRentalResponse>> completePayment(@PathVariable(name = "rentalId") @Positive Long rentalId,
																		  Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		return ResponseEntity.ok(ApiResponse.ok(rentalPaymentService.completePayment(rentalId, memberId)));
	}
	
	@PatchMapping("/{rentalItemId}/cancel")
	public ResponseEntity<ApiResponse<Void>> cancelRentalItem(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
															  Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		rentalLifecycleService.cancelRentalItem(rentalItemId, memberId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@PostMapping("/{rentalItemId}/return")
	public ResponseEntity<ApiResponse<RentalReturnResponse>> completeReturn(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
																			Authentication authentication,
																			@Valid @RequestBody RentalReturnRequest rentalReturnRequest) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		return ResponseEntity.ok(ApiResponse.ok(rentalLifecycleService.completeReturn(
				rentalReturnRequest.toCommand(rentalItemId, memberId))));
	}
	
	@PostMapping("/{rentalItemId}/refund")
	public ResponseEntity<ApiResponse<Void>> refundRental(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
														  Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		rentalLifecycleService.refundRentalItem(rentalItemId, memberId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@PostMapping("/{rentalItemId}/extend")
	public ResponseEntity<ApiResponse<Void>> extendRental(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
														  Authentication authentication,
														  @Valid @RequestBody ExtendRentalRequest request) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		rentalLifecycleService.extendRentalItem(request.toCommand(rentalItemId, memberId));
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@GetMapping("/{rentalId}")
	public ResponseEntity<ApiResponse<RentalResponse>> getRentalDetails(@PathVariable(name = "rentalId") @Positive Long rentalId,
																		Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		return ResponseEntity.ok(ApiResponse.ok(rentalQueryService.getRentalDetails(rentalId, memberId)));
	}
	
	@GetMapping
	public ResponseEntity<ApiResponse<List<RentalResponse>>> searchRentals(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) RentalStatus rentalStatus, Authentication authentication) {
		
		Long memberId = (Long) authentication.getPrincipal();
		
		return ResponseEntity.ok(ApiResponse.ok(rentalQueryService.searchRentals(startDate, endDate, rentalStatus, memberId)));
	}
}
