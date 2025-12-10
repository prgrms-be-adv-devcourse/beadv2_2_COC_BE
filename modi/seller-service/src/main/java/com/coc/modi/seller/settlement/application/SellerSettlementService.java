package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineResponse;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineCommand;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlementLine;
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

    public Page<SellerSettlementResponse> getSellerSettlements(Long sellerId, String periodYm, Pageable pageable) {
        Page<SellerSettlement> settlements;

        if (periodYm != null && !periodYm.isBlank()) {
            settlements = sellerSettlementRepository.findBySellerIdAndPeriodYm(sellerId, periodYm, pageable);
        } else {
            settlements = sellerSettlementRepository.findBySellerId(sellerId, pageable);
        }

        return settlements.map(SellerSettlementResponse::from);
    }

    public SellerSettlementResponse getSellerSettlement(Long sellerId, Long sellerSettlementId) {
        SellerSettlement settlement = findOwnedSettlement(sellerId, sellerSettlementId);
        return SellerSettlementResponse.from(settlement);
    }

    public List<SellerSettlementLineResponse> getSettlementLines(Long sellerId, Long sellerSettlementId) {
        SellerSettlement settlement = findOwnedSettlement(sellerId, sellerSettlementId);
        return settlement.getLines().stream()
                .map(SellerSettlementLineResponse::from)
                .toList();
    }

    @Transactional
    public SellerSettlementResponse recordSettlementLine(SellerSettlementLineCommand command) {
        SellerSettlement settlement = sellerSettlementRepository.findBySellerIdAndPeriodYm(
                        command.sellerId(),
                        command.periodYm())
                .orElseGet(() -> SellerSettlement.create(
                        command.batchId(),
                        command.sellerId(),
                        command.periodYm()));

        settlement.assignBatchIfAbsent(command.batchId());

        SellerSettlementLine line = SellerSettlementLine.of(
                command.sellerId(),
                command.rentalItemId(),
                command.memberId(),
                command.productId(),
                command.rentalAmount(),
                command.feeAmount()
        );

        settlement.addLineWithAggregation(line);

        SellerSettlement saved = sellerSettlementRepository.save(settlement);
        return SellerSettlementResponse.from(saved);
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
