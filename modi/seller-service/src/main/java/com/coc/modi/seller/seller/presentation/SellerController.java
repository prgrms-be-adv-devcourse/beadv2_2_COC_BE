package com.coc.modi.seller.seller.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.application.dto.SellerRentalResponse;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.application.dto.SellerIdResponse;
import com.coc.modi.seller.seller.presentation.dto.SellerCreateRequest;
import com.coc.modi.seller.seller.presentation.dto.SellerUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class SellerController {
	
	private final SellerService sellerService;
	
	@PostMapping("/api/sellers")
	public ResponseEntity<ApiResponse<SellerDetailResponse>> registerSeller(@Valid @RequestBody SellerCreateRequest request,
																	  @AuthenticationPrincipal CustomMember member) {
		
		
		SellerDetailResponse seller = sellerService.registerSeller(request.toCommand(member.getMemberId()));
  
		return ResponseEntity.ok(ApiResponse.ok(seller));
	}
	
	@GetMapping("/api/sellers/self")
	public ResponseEntity<ApiResponse<SellerDetailResponse>> getMySeller(@AuthenticationPrincipal CustomMember member) {
		
		
		SellerDetailResponse seller = sellerService.getSellerByMemberId(member.getMemberId());
  
		return ResponseEntity.ok(ApiResponse.ok(seller));
	}
	
	@GetMapping("/api/sellers/self/rentals")
	public ResponseEntity<ApiResponse<List<SellerRentalResponse>>> getMyRentals(@AuthenticationPrincipal CustomMember member,
																				@RequestParam(value = "productId", required = false) Long productId,
																				@RequestParam(value = "status") String status,
																				@RequestParam(value = "startDate") String startDate,
																				@RequestParam(value = "endDate") String endDate,
																				@RequestParam(value = "page", required = false) Integer page,
																				@RequestParam(value = "size", required = false) Integer size) {
		
		
		List<SellerRentalResponse> rentals = sellerService.getMyRentals(member.getMemberId(), productId, status, startDate, endDate, page, size);
		
		return ResponseEntity.ok(ApiResponse.ok(rentals));
	}
	
	@PutMapping("/api/sellers/self")
	public ResponseEntity<ApiResponse<SellerDetailResponse>> updateMySeller(@AuthenticationPrincipal CustomMember member,
																	  @Valid @RequestBody SellerUpdateRequest request) {
		
		
		SellerDetailResponse seller = sellerService.updateSellerByMemberId(member.getMemberId(), request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(seller));
	}
	
	// @GetMapping("/internal/sellers/by-member/{memberId}")
	// public SellerDetailResponse getSellerByMemberId(@PathVariable Long memberId) {
	//
	// 	return sellerService.getSellerByMemberId(memberId);
	// }

	@GetMapping("/internal/sellers/by-member/{memberId}")
	public SellerIdResponse getSellerInfo(@PathVariable Long memberId) {
		
		SellerDetailResponse seller = sellerService.getSellerByMemberId(memberId);
		
		return new SellerIdResponse(seller.sellerId(), seller.memberId());
	}
	
	@GetMapping("/internal/sellers/{sellerId}")
	public SellerDetailResponse getSellerById(@PathVariable Long sellerId) {
		
		return sellerService.getSeller(sellerId);
	}
}
