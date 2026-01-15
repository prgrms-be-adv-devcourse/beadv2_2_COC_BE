package com.coc.modi.seller.seller.presentation.admin;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.seller.seller.application.SellerApprovalService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sellers")
public class SellerApprovalAdminController {

	private final SellerApprovalService sellerApprovalService;

	@PatchMapping("/{sellerId}/approve")
	public SellerDetailResponse approveSeller(@PathVariable Long sellerId) {

		return sellerApprovalService.approveSeller(sellerId);
	}

	@PatchMapping("/{sellerId}/reject")
	public SellerDetailResponse rejectSeller(@PathVariable Long sellerId) {

		return sellerApprovalService.rejectSeller(sellerId);
	}
}
