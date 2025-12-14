package com.coc.modi.rental.rental.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
																  @AuthenticationPrincipal CustomMember member) {
		
		rentalCreationService.createRentalFromCart(rentalFromCartRequest.toCommand(member.getMemberId()));
		
		return ResponseEntity.status(201).body(ApiResponse.ok(null));
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createRental(@Valid @RequestBody RentalRequest rentalRequest,
														  @AuthenticationPrincipal CustomMember member) {
		
		rentalCreationService.createRental(rentalRequest.toCommand(member.getMemberId()));
		
		return ResponseEntity.status(201).body(ApiResponse.ok(null));
	}
	
	@PatchMapping("/{rentalItemId}/accept")
	public ResponseEntity<ApiResponse<Void>> acceptRentalItem(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
															  @AuthenticationPrincipal CustomMember member) {
		
		rentalDecisionService.acceptRentalItem(rentalItemId, member.getMemberId());
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@PatchMapping("/{rentalItemId}/reject")
	public ResponseEntity<ApiResponse<Void>> rejectRentalItem(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
															  @AuthenticationPrincipal CustomMember member) {
		
		rentalDecisionService.rejectRentalItem(rentalItemId, member.getMemberId());
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@PostMapping("/{rentalId}/pay")
	public ResponseEntity<ApiResponse<PayRentalResponse>> completePayment(@PathVariable(name = "rentalId") @Positive Long rentalId,
																		  @AuthenticationPrincipal CustomMember member) {
		
		return ResponseEntity.ok(ApiResponse.ok(rentalPaymentService.completePayment(rentalId, member.getMemberId())));
	}
	
	@PatchMapping("/{rentalItemId}/cancel")
	public ResponseEntity<ApiResponse<Void>> cancelRentalItem(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
															  @AuthenticationPrincipal CustomMember member) {
		
		rentalLifecycleService.cancelRentalItem(rentalItemId, member.getMemberId());
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@PostMapping("/{rentalItemId}/return")
	public ResponseEntity<ApiResponse<RentalReturnResponse>> completeReturn(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
																			@AuthenticationPrincipal CustomMember member,
																			@Valid @RequestBody RentalReturnRequest rentalReturnRequest) {
		
		return ResponseEntity.ok(ApiResponse.ok(rentalLifecycleService.completeReturn(
				rentalReturnRequest.toCommand(rentalItemId, member.getMemberId()))));
	}
	
	@PostMapping("/{rentalItemId}/refund")
	public ResponseEntity<ApiResponse<Void>> refundRental(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
														  @AuthenticationPrincipal CustomMember member) {
		
		rentalLifecycleService.refundRentalItem(rentalItemId, member.getMemberId());
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@PostMapping("/{rentalItemId}/extend")
	public ResponseEntity<ApiResponse<Void>> extendRental(@PathVariable(name = "rentalItemId") @Positive Long rentalItemId,
														  @AuthenticationPrincipal CustomMember member,
														  @Valid @RequestBody ExtendRentalRequest request) {
		
		rentalLifecycleService.extendRentalItem(request.toCommand(rentalItemId, member.getMemberId()));
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	@GetMapping("/{rentalId}")
	public ResponseEntity<ApiResponse<RentalResponse>> getRentalDetails(@PathVariable(name = "rentalId") @Positive Long rentalId,
																		@AuthenticationPrincipal CustomMember member) {
		
		return ResponseEntity.ok(ApiResponse.ok(rentalQueryService.getRentalDetails(rentalId, member.getMemberId())));
	}
	
	@GetMapping
	public ResponseEntity<ApiResponse<List<RentalResponse>>> searchRentals(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) RentalStatus rentalStatus, @AuthenticationPrincipal CustomMember member) {
		
		return ResponseEntity.ok(ApiResponse.ok(rentalQueryService.searchRentals(startDate, endDate, rentalStatus, member.getMemberId())));
	}
}
