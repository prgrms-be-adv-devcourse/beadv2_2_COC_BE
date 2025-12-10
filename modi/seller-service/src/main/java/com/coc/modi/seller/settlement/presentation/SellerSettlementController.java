package com.coc.modi.seller.settlement.presentation;

import com.coc.modi.seller.settlement.application.SellerSettlementService;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineResponse;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.seller.application.dto.SellerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class SellerSettlementController {

    private final SellerSettlementService sellerSettlementService;
    private final SellerService sellerService;

    @GetMapping("/api/settlements/sellers/me")
    public ResponseEntity<ApiResponse<Page<SellerSettlementResponse>>> getMySettlements(Authentication authentication,
                                                                                        @RequestParam(value = "periodYm", required = false) String periodYm,
                                                                                        Pageable pageable) {
        Long memberId = (Long) authentication.getPrincipal();
        SellerResponse seller = sellerService.getSellerByMemberId(memberId);
        Page<SellerSettlementResponse> settlements = sellerSettlementService.getSellerSettlements(seller.id(), periodYm, pageable);
        return ResponseEntity.ok(ApiResponse.ok(settlements));
    }

    @GetMapping("/api/settlements/sellers/me/{sellerSettlementId}")
    public ResponseEntity<ApiResponse<SellerSettlementResponse>> getMySettlement(Authentication authentication,
                                                                                 @PathVariable Long sellerSettlementId) {
        Long memberId = (Long) authentication.getPrincipal();
        SellerResponse seller = sellerService.getSellerByMemberId(memberId);
        SellerSettlementResponse settlement = sellerSettlementService.getSellerSettlement(seller.id(), sellerSettlementId);
        return ResponseEntity.ok(ApiResponse.ok(settlement));
    }

    @GetMapping("/api/settlements/sellers/me/{sellerSettlementId}/lines")
    public ResponseEntity<ApiResponse<List<SellerSettlementLineResponse>>> getMySettlementLines(Authentication authentication,
                                                                                                @PathVariable Long sellerSettlementId) {
        Long memberId = (Long) authentication.getPrincipal();
        SellerResponse seller = sellerService.getSellerByMemberId(memberId);
        List<SellerSettlementLineResponse> lines = sellerSettlementService.getSettlementLines(seller.id(), sellerSettlementId);
        return ResponseEntity.ok(ApiResponse.ok(lines));
    }
}
