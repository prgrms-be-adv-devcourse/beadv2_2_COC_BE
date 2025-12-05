package com.coc.modi.seller.seller.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.seller.application.dto.SellerResponse;
import com.coc.modi.seller.seller.presentation.dto.SellerCreateRequest;
import com.coc.modi.seller.seller.presentation.dto.SellerUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/api/sellers")
    public ResponseEntity<ApiResponse<SellerResponse>> registerSeller(@Valid @RequestBody SellerCreateRequest request,
                                                                      Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        SellerResponse seller = sellerService.registerSeller(request.toCommand(memberId));
        return ResponseEntity.ok(ApiResponse.ok(seller));
    }

    @GetMapping("/api/sellers/me")
    public ResponseEntity<ApiResponse<SellerResponse>> getMySeller(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        SellerResponse seller = sellerService.getSellerByMemberId(memberId);
        return ResponseEntity.ok(ApiResponse.ok(seller));
    }

    @PutMapping("/api/sellers/me")
    public ResponseEntity<ApiResponse<SellerResponse>> updateMySeller(Authentication authentication,
                                                                      @Valid @RequestBody SellerUpdateRequest request) {
        Long memberId = (Long) authentication.getPrincipal();
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
