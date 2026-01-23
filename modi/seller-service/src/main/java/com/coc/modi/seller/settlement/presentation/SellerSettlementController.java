package com.coc.modi.seller.settlement.presentation;

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
}
