package com.coc.modi.seller.settlement.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.settlement.application.SellerSettlementService;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineResponse;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.seller.application.dto.SellerResponse;
import com.coc.modi.seller.exception.SettlementInputInvalidException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class SellerSettlementController {
	
	private final SellerSettlementService sellerSettlementService;
	private final SellerService sellerService;
	
	private static final DateTimeFormatter PAID_AT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	
	@GetMapping("/api/settlements/sellers/self")
	public ResponseEntity<ApiResponse<Page<SellerSettlementResponse>>> getMySettlements(@AuthenticationPrincipal CustomMember member,
																						@RequestParam(value = "periodYm", required = false) String periodYm,
																						Pageable pageable) {
		
		SellerResponse seller = sellerService.getSellerByMemberId(member.getMemberId());
		Page<SellerSettlementResponse> settlements = sellerSettlementService.getSellerSettlements(seller.id(), periodYm, pageable);
		return ResponseEntity.ok(ApiResponse.ok(settlements));
	}
	
	@GetMapping("/api/settlements/sellers/self/{sellerSettlementId}")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> getMySettlement(@AuthenticationPrincipal CustomMember member,
																				 @PathVariable Long sellerSettlementId) {
		
		
		SellerResponse seller = sellerService.getSellerByMemberId(member.getMemberId());
		SellerSettlementResponse settlement = sellerSettlementService.getSellerSettlement(seller.id(), sellerSettlementId);
		return ResponseEntity.ok(ApiResponse.ok(settlement));
	}
	
	@GetMapping("/api/settlements/sellers/self/{sellerSettlementId}/lines")
	public ResponseEntity<ApiResponse<List<SellerSettlementLineResponse>>> getMySettlementLines(@AuthenticationPrincipal CustomMember member,
																								@PathVariable Long sellerSettlementId) {
		
		
		SellerResponse seller = sellerService.getSellerByMemberId(member.getMemberId());
		List<SellerSettlementLineResponse> lines = sellerSettlementService.getSettlementLines(seller.id(), sellerSettlementId);
		return ResponseEntity.ok(ApiResponse.ok(lines));
	}
	
	@PostMapping("/api/settlements/sellers/self/{sellerSettlementId}/pay")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> payMySettlement(@AuthenticationPrincipal CustomMember member,
																				 @PathVariable Long sellerSettlementId,
																				 @RequestParam(value = "paidAt", required = false) String paidAt) {
		
		
		SellerResponse seller = sellerService.getSellerByMemberId(member.getMemberId());
		LocalDateTime paidAtValue = paidAt != null ? parsePaidAt(paidAt) : LocalDateTime.now();
		SellerSettlementResponse settlement = sellerSettlementService.markAsPaid(seller.id(), sellerSettlementId, paidAtValue);
		return ResponseEntity.ok(ApiResponse.ok(settlement));
	}
	
	@PostMapping("/api/settlements/sellers/self/{sellerSettlementId}/cancel")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> cancelMySettlement(@AuthenticationPrincipal CustomMember member,
																					@PathVariable Long sellerSettlementId) {
		
		
		SellerResponse seller = sellerService.getSellerByMemberId(member.getMemberId());
		SellerSettlementResponse settlement = sellerSettlementService.cancelSettlement(seller.id(), sellerSettlementId);
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
