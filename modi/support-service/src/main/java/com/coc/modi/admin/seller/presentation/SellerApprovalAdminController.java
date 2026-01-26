package com.coc.modi.admin.seller.presentation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.admin.exception.AdminAccessDeniedException;
import com.coc.modi.admin.seller.application.SellerApprovalAdminService;
import com.coc.modi.admin.seller.application.dto.SellerRegistrationResponse;
import com.coc.modi.admin.seller.domain.SellerRegistrationStatus;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sellers")
public class SellerApprovalAdminController {

	private final SellerApprovalAdminService sellerApprovalAdminService;

	@PatchMapping("/{memberId}/approve")
	public ResponseEntity<ApiResponse<SellerRegistrationResponse>> approveSeller(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long memberId
	) {

		requireAdmin(member);
		SellerRegistrationResponse response = sellerApprovalAdminService.approveSeller(memberId, member.memberId());
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@PatchMapping("/{memberId}/reject")
	public ResponseEntity<ApiResponse<SellerRegistrationResponse>> rejectSeller(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long memberId
	) {

		requireAdmin(member);
		SellerRegistrationResponse response = sellerApprovalAdminService.rejectSeller(memberId);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@GetMapping("/registrations")
	public ResponseEntity<ApiResponse<Page<SellerRegistrationResponse>>> getRegistrations(
			@AuthenticationPrincipal CustomMember member,
			@RequestParam(value = "status", required = false) SellerRegistrationStatus status,
			Pageable pageable
	) {

		requireAdmin(member);
		Page<SellerRegistrationResponse> response = sellerApprovalAdminService.getRegistrations(status, pageable);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	private void requireAdmin(CustomMember member) {
		if (member != null && "ADMIN".equals(member.role())) {
			return;
		}
		if (SecurityContextHolder.getContext().getAuthentication() != null
				&& SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
						.anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority())
								|| "ADMIN".equals(auth.getAuthority()))) {
			return;
		}
		throw new AdminAccessDeniedException("관리자 권한이 필요합니다.");
	}
}
