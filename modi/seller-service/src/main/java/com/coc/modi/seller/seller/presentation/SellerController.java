package com.coc.modi.seller.seller.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.seller.application.SellerProductService;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.seller.application.dto.SellerRentalResponse;
import com.coc.modi.seller.seller.infrastructure.client.product.dto.ProductSummaryResponse;
import com.coc.modi.seller.seller.presentation.dto.SellerCreateRequest;
import com.coc.modi.seller.seller.presentation.dto.SellerUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers")
public class SellerController {
	
	private final SellerService sellerService;
	private final SellerProductService sellerProductService;
	
	@PostMapping
	public ResponseEntity<ApiResponse<SellerDetailResponse>> registerSeller(@Valid @RequestBody SellerCreateRequest request,
																			@AuthenticationPrincipal CustomMember member) {
		
		SellerDetailResponse seller = sellerService.registerSeller(request.toCommand(member.memberId()));
		
		return ResponseEntity.ok(ApiResponse.ok(seller));
	}
	
	@GetMapping("/self")
	public ResponseEntity<ApiResponse<SellerDetailResponse>> getMySeller(@AuthenticationPrincipal CustomMember member) {
		
		SellerDetailResponse seller = sellerService.getSellerByMemberId(member.memberId());
		
		return ResponseEntity.ok(ApiResponse.ok(seller));
	}
	
	//추후 Query로 변경 고려
	@GetMapping("/self/rentals")
	public ResponseEntity<ApiResponse<List<SellerRentalResponse>>> getMyRentals(@AuthenticationPrincipal CustomMember member,
																				@RequestParam(value = "productId", required = false) Long productId,
																				@RequestParam(value = "status") String status,
																				@RequestParam(value = "startDate") String startDate,
																				@RequestParam(value = "endDate") String endDate,
																				@RequestParam(value = "page", required = false) Integer page,
																				@RequestParam(value = "size", required = false) Integer size) {
		
		List<SellerRentalResponse> rentals = sellerService.getMyRentals(member.memberId(), productId, status, startDate, endDate, page, size);
		
		return ResponseEntity.ok(ApiResponse.ok(rentals));
	}
	
	@PutMapping("/self")
	public ResponseEntity<ApiResponse<SellerDetailResponse>> updateMySeller(@AuthenticationPrincipal CustomMember member,
																			@Valid @RequestBody SellerUpdateRequest request) {
		
		SellerDetailResponse seller = sellerService.updateSellerByMemberId(member.memberId(), request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(seller));
	}
	
	@GetMapping("/products/{productId}")
	public ResponseEntity<ApiResponse<ProductSummaryResponse>> getProduct(@PathVariable Long productId) {
		
		ProductSummaryResponse response = sellerProductService.getProductSummary(productId);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
}
