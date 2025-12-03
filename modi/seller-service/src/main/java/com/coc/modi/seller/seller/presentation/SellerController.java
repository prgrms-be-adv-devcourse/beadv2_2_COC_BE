package com.coc.modi.seller.seller.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.seller.application.dto.SellerInfo;
import com.coc.modi.seller.seller.presentation.dto.SellerCreateRequest;
import com.coc.modi.seller.seller.presentation.dto.SellerUpdateRequest;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/api/sellers")
    public ApiResponse<SellerInfo> registerSeller(@RequestHeader("X-Member-Id") Long memberId,
                                                  @Valid @RequestBody SellerCreateRequest request) {
        SellerInfo seller = sellerService.registerSeller(request.toCommand(memberId));
        return ApiResponse.ok(seller);
    }

    @GetMapping("/api/sellers/me")
    public ApiResponse<SellerInfo> getMySeller(@RequestHeader("X-Member-Id") Long memberId) {
        SellerInfo seller = sellerService.getSellerByMemberId(memberId);
        return ApiResponse.ok(seller);
    }

    @PutMapping("/api/sellers/me")
    public ApiResponse<SellerInfo> updateMySeller(@RequestHeader("X-Member-Id") Long memberId,
                                                  @Valid @RequestBody SellerUpdateRequest request) {
        SellerInfo seller = sellerService.updateSellerByMemberId(memberId, request.toCommand());
        return ApiResponse.ok(seller);
    }

    @GetMapping("/internal/sellers/by-member/{memberId}")
    public ApiResponse<SellerInfo> getSellerByMemberId(@PathVariable Long memberId) {
        SellerInfo seller = sellerService.getSellerByMemberId(memberId);
        return ApiResponse.ok(seller);
    }

    @GetMapping("/internal/sellers/{sellerId}")
    public ApiResponse<SellerInfo> getSellerById(@PathVariable Long sellerId) {
        SellerInfo seller = sellerService.getSeller(sellerId);
        return ApiResponse.ok(seller);
    }
}
