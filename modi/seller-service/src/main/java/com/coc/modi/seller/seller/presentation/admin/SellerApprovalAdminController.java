package com.coc.modi.seller.seller.presentation.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.seller.application.SellerApprovalService;
import com.coc.modi.seller.seller.application.dto.SellerRegistrationResponse;
import com.coc.modi.seller.seller.exception.SellerException;
import com.coc.modi.seller.seller.registration.domain.SellerRegistrationStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sellers")
public class SellerApprovalAdminController {

	private final SellerApprovalService sellerApprovalService;

	@PatchMapping("/{memberId}/approve")
	public SellerRegistrationResponse approveSeller(@AuthenticationPrincipal CustomMember member,
											  @PathVariable Long memberId) {
		
		requireAdmin(member);
		return sellerApprovalService.approveSeller(memberId, member.memberId());
	}

	@PatchMapping("/{memberId}/reject")
	public SellerRegistrationResponse rejectSeller(@AuthenticationPrincipal CustomMember member,
											 @PathVariable Long memberId) {

		requireAdmin(member);
		return sellerApprovalService.rejectSeller(memberId);
	}

	@GetMapping("/registrations")
	public ResponseEntity<ApiResponse<Page<SellerRegistrationResponse>>> getRegistrations(
			@AuthenticationPrincipal CustomMember member,
			@RequestParam(value = "status", required = false) SellerRegistrationStatus status,
			Pageable pageable) {

		requireAdmin(member);
		Page<SellerRegistrationResponse> response = sellerApprovalService.getRegistrations(status, pageable);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	private void requireAdmin(CustomMember member) {

		if (member == null || !"ADMIN".equals(member.role())) {
			throw new SellerException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
		}
	}
}
