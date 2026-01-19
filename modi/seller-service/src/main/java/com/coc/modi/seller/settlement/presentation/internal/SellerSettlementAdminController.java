package com.coc.modi.seller.settlement.presentation.internal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.settlement.application.SellerSettlementService;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import com.coc.modi.seller.settlement.exception.SettlementAccessDeniedException;
import com.coc.modi.seller.settlement.exception.SettlementInputInvalidException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/settlements/seller-settlements")
public class SellerSettlementAdminController {

	private static final String ADMIN_ROLE = "ROLE_ADMIN";
	private static final DateTimeFormatter PAID_AT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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

	@PostMapping("/{sellerSettlementId}/pay")
	public ApiResponse<SellerSettlementResponse> paySellerSettlement(
			@PathVariable Long sellerSettlementId,
			@RequestParam(value = "paidAt", required = false) String paidAt,
			Authentication authentication
	) {

		requireAdmin(authentication);
		LocalDateTime paidAtValue = paidAt != null ? parsePaidAt(paidAt) : LocalDateTime.now();
		return ApiResponse.ok(sellerSettlementService.requestPayoutByAdmin(sellerSettlementId, paidAtValue));
	}

	private void requireAdmin(Authentication authentication) {

		if (authentication == null || authentication.getAuthorities().stream()
				.noneMatch(authority -> ADMIN_ROLE.equalsIgnoreCase(authority.getAuthority()))) {
			throw new SettlementAccessDeniedException("관리자 권한이 필요합니다.");
		}
	}

	private LocalDateTime parsePaidAt(String paidAt) {

		try {
			return LocalDateTime.parse(paidAt, PAID_AT_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new SettlementInputInvalidException(
					"paidAt must be ISO-8601 format, e.g. 2024-12-31T23:59:59",
					e
			);
		}
	}
}
