package com.coc.modi.seller.settlement.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.settlement.application.SellerSettlementService;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineResponse;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class SellerSettlementController {

    private final SellerSettlementService sellerSettlementService;

    @GetMapping("/api/settlements/sellers/me")
    public ResponseEntity<ApiResponse<Page<SellerSettlementResponse>>> getMySettlements(@RequestHeader("X-Member-Id") Long sellerId,
                                                                                        @RequestParam(value = "periodYm", required = false) String periodYm,
                                                                                        Pageable pageable) {
        Page<SellerSettlementResponse> settlements = sellerSettlementService.getSellerSettlements(sellerId, periodYm, pageable);
        return ResponseEntity.ok(ApiResponse.ok(settlements));
    }

    @GetMapping("/api/settlements/sellers/me/{sellerSettlementId}")
    public ResponseEntity<ApiResponse<SellerSettlementResponse>> getMySettlement(@RequestHeader("X-Member-Id") Long sellerId,
                                                                                 @PathVariable Long sellerSettlementId) {
        SellerSettlementResponse settlement = sellerSettlementService.getSellerSettlement(sellerId, sellerSettlementId);
        return ResponseEntity.ok(ApiResponse.ok(settlement));
    }

    @GetMapping("/api/settlements/sellers/me/{sellerSettlementId}/lines")
    public ResponseEntity<ApiResponse<List<SellerSettlementLineResponse>>> getMySettlementLines(@RequestHeader("X-Member-Id") Long sellerId,
                                                                                                @PathVariable Long sellerSettlementId) {
        List<SellerSettlementLineResponse> lines = sellerSettlementService.getSettlementLines(sellerId, sellerSettlementId);
        return ResponseEntity.ok(ApiResponse.ok(lines));
    }
}
