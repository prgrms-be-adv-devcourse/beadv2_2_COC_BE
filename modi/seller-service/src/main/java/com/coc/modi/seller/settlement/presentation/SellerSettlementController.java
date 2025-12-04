package com.coc.modi.seller.settlement.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.settlement.application.SellerSettlementService;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementInfo;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ApiResponse<Page<SellerSettlementInfo>> getMySettlements(@RequestHeader("X-Member-Id") Long sellerId,
                                                                    @RequestParam(value = "periodYm", required = false) String periodYm,
                                                                    Pageable pageable) {
        Page<SellerSettlementInfo> settlements = sellerSettlementService.getSellerSettlements(sellerId, periodYm, pageable);
        return ApiResponse.ok(settlements);
    }

    @GetMapping("/api/settlements/sellers/me/{sellerSettlementId}")
    public ApiResponse<SellerSettlementInfo> getMySettlement(@RequestHeader("X-Member-Id") Long sellerId,
                                                             @PathVariable Long sellerSettlementId) {
        SellerSettlementInfo settlement = sellerSettlementService.getSellerSettlement(sellerId, sellerSettlementId);
        return ApiResponse.ok(settlement);
    }

    @GetMapping("/api/settlements/sellers/me/{sellerSettlementId}/lines")
    public ApiResponse<List<SellerSettlementLineInfo>> getMySettlementLines(@RequestHeader("X-Member-Id") Long sellerId,
                                                                            @PathVariable Long sellerSettlementId) {
        List<SellerSettlementLineInfo> lines = sellerSettlementService.getSettlementLines(sellerId, sellerSettlementId);
        return ApiResponse.ok(lines);
    }
}
