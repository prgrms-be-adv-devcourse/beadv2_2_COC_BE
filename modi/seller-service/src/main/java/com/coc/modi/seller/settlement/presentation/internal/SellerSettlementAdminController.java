package com.coc.modi.seller.settlement.presentation.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.settlement.application.SellerSettlementService;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import com.coc.modi.seller.settlement.exception.SettlementAccessDeniedException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/settlements/seller-settlements")
public class SellerSettlementAdminController {

	private static final String ADMIN_ROLE = "ROLE_ADMIN";

	private final SellerSettlementService sellerSettlementService;

	@GetMapping
	public ApiResponse<Page<SellerSettlementResponse>> getSellerSettlements(
			@RequestParam(value = "periodYm", required = false) String periodYm,
			Pageable pageable,
			Authentication authentication
	) {

		requireAdmin(authentication);
		return ApiResponse.ok(sellerSettlementService.getAllSettlements(periodYm, pageable));
	}

	private void requireAdmin(Authentication authentication) {

		if (authentication == null || authentication.getAuthorities().stream()
				.noneMatch(authority -> ADMIN_ROLE.equalsIgnoreCase(authority.getAuthority()))) {
			throw new SettlementAccessDeniedException("관리자 권한이 필요합니다.");
		}
	}
}
