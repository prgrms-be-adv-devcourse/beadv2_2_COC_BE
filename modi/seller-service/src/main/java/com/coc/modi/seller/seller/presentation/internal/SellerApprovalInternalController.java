package com.coc.modi.seller.seller.presentation.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.seller.seller.application.SellerApprovalService;
import com.coc.modi.seller.seller.application.dto.SellerRegistrationPageResponse;
import com.coc.modi.seller.seller.application.dto.SellerRegistrationResponse;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/sellers")
public class SellerApprovalInternalController {

	private final SellerApprovalService sellerApprovalService;

	@PatchMapping("/{memberId}/approve")
	public SellerRegistrationResponse approveSeller(
			@PathVariable Long memberId,
			@RequestParam("approvedBy") Long approvedBy
	) {

		return sellerApprovalService.approveSeller(memberId, approvedBy);
	}

	@PatchMapping("/{memberId}/reject")
	public SellerRegistrationResponse rejectSeller(@PathVariable Long memberId) {

		return sellerApprovalService.rejectSeller(memberId);
	}

	@GetMapping("/registrations")
	public SellerRegistrationPageResponse getRegistrations(
			@RequestParam(value = "status", required = false) SellerRegistrationStatus status,
			Pageable pageable
	) {

		Page<SellerRegistrationResponse> response = sellerApprovalService.getRegistrations(status, pageable);
		return SellerRegistrationPageResponse.from(response);
	}
}
