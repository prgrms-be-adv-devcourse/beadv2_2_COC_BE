package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.application.dto.SellerSettlementInfo;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineInfo;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerSettlementService {

    private final SellerSettlementRepository sellerSettlementRepository;

    public Page<SellerSettlementInfo> getSellerSettlements(Long sellerId, Pageable pageable) {
        return sellerSettlementRepository.findBySellerId(sellerId, pageable)
                .map(SellerSettlementInfo::from);
    }

    public SellerSettlementInfo getSellerSettlement(Long sellerId, Long sellerSettlementId) {
        SellerSettlement settlement = findOwnedSettlement(sellerId, sellerSettlementId);
        return SellerSettlementInfo.from(settlement);
    }

    public List<SellerSettlementLineInfo> getSettlementLines(Long sellerId, Long sellerSettlementId) {
        SellerSettlement settlement = findOwnedSettlement(sellerId, sellerSettlementId);
        return settlement.getLines().stream()
                .map(SellerSettlementLineInfo::from)
                .toList();
    }

    private SellerSettlement findOwnedSettlement(Long sellerId, Long sellerSettlementId) {
        SellerSettlement settlement = sellerSettlementRepository.findById(sellerSettlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found. id=" + sellerSettlementId));

        if (!settlement.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("Settlement does not belong to seller. sellerId=" + sellerId);
        }
        return settlement;
    }
}
