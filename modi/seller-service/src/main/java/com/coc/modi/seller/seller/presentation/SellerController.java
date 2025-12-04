package com.coc.modi.seller.seller.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.seller.application.dto.SellerResponse;
import com.coc.modi.seller.seller.presentation.dto.SellerCreateRequest;
import com.coc.modi.seller.seller.presentation.dto.SellerUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/api/sellers")
    public ResponseEntity<ApiResponse<SellerResponse>> registerSeller(@RequestHeader("X-Member-Id") Long memberId,
                                                                      @Valid @RequestBody SellerCreateRequest request) {
        SellerResponse seller = sellerService.registerSeller(request.toCommand(memberId));
        return ResponseEntity.ok(ApiResponse.ok(seller));
    }

    @GetMapping("/api/sellers/me")
    public ResponseEntity<ApiResponse<SellerResponse>> getMySeller(@RequestHeader("X-Member-Id") Long memberId) {
        SellerResponse seller = sellerService.getSellerByMemberId(memberId);
        return ResponseEntity.ok(ApiResponse.ok(seller));
    }

    @PutMapping("/api/sellers/me")
    public ResponseEntity<ApiResponse<SellerResponse>> updateMySeller(@RequestHeader("X-Member-Id") Long memberId,
                                                                      @Valid @RequestBody SellerUpdateRequest request) {
        SellerResponse seller = sellerService.updateSellerByMemberId(memberId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.ok(seller));
    }

    @GetMapping("/internal/sellers/by-member/{memberId}")
    public SellerResponse getSellerByMemberId(@PathVariable Long memberId) {
        return sellerService.getSellerByMemberId(memberId);
    }

    @GetMapping("/internal/sellers/{sellerId}")
    public SellerResponse getSellerById(@PathVariable Long sellerId) {
        return sellerService.getSeller(sellerId);
    }
}
