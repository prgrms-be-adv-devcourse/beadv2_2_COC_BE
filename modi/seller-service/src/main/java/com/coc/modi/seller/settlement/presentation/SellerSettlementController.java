package com.coc.modi.seller.settlement.presentation;

import com.coc.modi.common.ApiResponse;
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
	public ResponseEntity<ApiResponse<Page<SellerSettlementResponse>>> getMySettlements(Authentication authentication,
																						@RequestParam(value = "periodYm", required = false) String periodYm,
																						Pageable pageable) {
		
		Long memberId = (Long)authentication.getPrincipal();
		SellerResponse seller = sellerService.getSellerByMemberId(memberId);
		Page<SellerSettlementResponse> settlements = sellerSettlementService.getSellerSettlements(seller.sellerId(), periodYm, pageable);
		return ResponseEntity.ok(ApiResponse.ok(settlements));
	}
	
	@GetMapping("/api/settlements/sellers/self/{sellerSettlementId}")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> getMySettlement(Authentication authentication,
																				 @PathVariable Long sellerSettlementId) {
		
		Long memberId = (Long)authentication.getPrincipal();
		SellerResponse seller = sellerService.getSellerByMemberId(memberId);
		SellerSettlementResponse settlement = sellerSettlementService.getSellerSettlement(seller.sellerId(), sellerSettlementId);
		return ResponseEntity.ok(ApiResponse.ok(settlement));
	}
	
	@GetMapping("/api/settlements/sellers/self/{sellerSettlementId}/lines")
	public ResponseEntity<ApiResponse<List<SellerSettlementLineResponse>>> getMySettlementLines(Authentication authentication,
																								@PathVariable Long sellerSettlementId) {
		
		Long memberId = (Long)authentication.getPrincipal();
		SellerResponse seller = sellerService.getSellerByMemberId(memberId);
		List<SellerSettlementLineResponse> lines = sellerSettlementService.getSettlementLines(seller.sellerId(), sellerSettlementId);
		return ResponseEntity.ok(ApiResponse.ok(lines));
	}
	
	@PostMapping("/api/settlements/sellers/self/{sellerSettlementId}/pay")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> payMySettlement(Authentication authentication,
																				 @PathVariable Long sellerSettlementId,
																				 @RequestParam(value = "paidAt", required = false) String paidAt) {
		
		Long memberId = (Long)authentication.getPrincipal();
		SellerResponse seller = sellerService.getSellerByMemberId(memberId);
		LocalDateTime paidAtValue = paidAt != null ? parsePaidAt(paidAt) : LocalDateTime.now();
		SellerSettlementResponse settlement = sellerSettlementService.markAsPaid(seller.sellerId(), sellerSettlementId, paidAtValue);
		return ResponseEntity.ok(ApiResponse.ok(settlement));
	}
	
	@PostMapping("/api/settlements/sellers/self/{sellerSettlementId}/cancel")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> cancelMySettlement(Authentication authentication,
																					@PathVariable Long sellerSettlementId) {
		
		Long memberId = (Long)authentication.getPrincipal();
		SellerResponse seller = sellerService.getSellerByMemberId(memberId);
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
