package com.coc.modi.seller.seller.presentation.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.seller.application.SellerApprovalService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.exception.SellerException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sellers")
public class SellerApprovalAdminController {

	private final SellerApprovalService sellerApprovalService;

	@PatchMapping("/{sellerId}/approve")
	public SellerDetailResponse approveSeller(@AuthenticationPrincipal CustomMember member,
											  @PathVariable Long sellerId) {
		
		requireAdmin(member);
		return sellerApprovalService.approveSeller(sellerId);
	}

	@PatchMapping("/{sellerId}/reject")
	public SellerDetailResponse rejectSeller(@AuthenticationPrincipal CustomMember member,
											 @PathVariable Long sellerId) {

		requireAdmin(member);
		return sellerApprovalService.rejectSeller(sellerId);
	}

	private void requireAdmin(CustomMember member) {

		if (member == null || !"ADMIN".equals(member.role())) {
			throw new SellerException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
		}
	}
}
