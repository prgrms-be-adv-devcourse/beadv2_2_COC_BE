package com.coc.modi.seller.settlement.presentation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.exception.SettlementInputInvalidException;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.settlement.application.SellerSettlementService;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineResponse;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements/sellers/self")
public class SellerSettlementController {
	
	private final SellerSettlementService sellerSettlementService;
	private final SellerService sellerService;
	
	private static final DateTimeFormatter PAID_AT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	
	@GetMapping
	public ResponseEntity<ApiResponse<Page<SellerSettlementResponse>>> getMySettlements(@AuthenticationPrincipal CustomMember member,
																						@RequestParam(value = "periodYm", required = false) String periodYm,
																						Pageable pageable) {
		
		
		SellerDetailResponse seller = sellerService.getSellerByMemberId(member.memberId());
		Page<SellerSettlementResponse> settlements = sellerSettlementService.getSellerSettlements(seller.sellerId(), periodYm, pageable);
		return ResponseEntity.ok(ApiResponse.ok(settlements));
	}
	
	@GetMapping("/{sellerSettlementId}")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> getMySettlement(@AuthenticationPrincipal CustomMember member,
																				 @PathVariable Long sellerSettlementId) {
		
		SellerDetailResponse seller = sellerService.getSellerByMemberId(member.memberId());
		SellerSettlementResponse settlement = sellerSettlementService.getSellerSettlement(seller.sellerId(), sellerSettlementId);
		return ResponseEntity.ok(ApiResponse.ok(settlement));
	}
	
	@GetMapping("/{sellerSettlementId}/lines")
	public ResponseEntity<ApiResponse<List<SellerSettlementLineResponse>>> getMySettlementLines(@AuthenticationPrincipal CustomMember member,
																								@PathVariable Long sellerSettlementId) {
		
		SellerDetailResponse seller = sellerService.getSellerByMemberId(member.memberId());
		List<SellerSettlementLineResponse> lines = sellerSettlementService.getSettlementLines(seller.sellerId(), sellerSettlementId);
		return ResponseEntity.ok(ApiResponse.ok(lines));
	}
	
	@PostMapping("/{sellerSettlementId}/pay")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> payMySettlement(@AuthenticationPrincipal CustomMember member,
																				 @PathVariable Long sellerSettlementId,
																				 @RequestParam(value = "paidAt", required = false) String paidAt) {
		
		
		SellerDetailResponse seller = sellerService.getSellerByMemberId(member.memberId());
		LocalDateTime paidAtValue = paidAt != null ? parsePaidAt(paidAt) : LocalDateTime.now();
		SellerSettlementResponse settlement = sellerSettlementService.markAsPaid(seller.sellerId(), sellerSettlementId, paidAtValue);
		return ResponseEntity.ok(ApiResponse.ok(settlement));
	}
	
	@PostMapping("/{sellerSettlementId}/cancel")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> cancelMySettlement(@AuthenticationPrincipal CustomMember member,
																					@PathVariable Long sellerSettlementId) {
		
		SellerDetailResponse seller = sellerService.getSellerByMemberId(member.memberId());
		SellerSettlementResponse settlement = sellerSettlementService.cancelSettlement(seller.sellerId(), sellerSettlementId);
		return ResponseEntity.ok(ApiResponse.ok(settlement));
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
