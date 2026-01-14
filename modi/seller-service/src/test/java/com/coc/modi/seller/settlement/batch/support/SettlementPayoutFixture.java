package com.coc.modi.seller.settlement.batch.support;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementLine;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.domain.SettlementBatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public final class SettlementPayoutFixture {

    private static final AtomicLong RENTAL_ITEM_SEQ = new AtomicLong(1000);
    private static final AtomicLong PRODUCT_SEQ = new AtomicLong(5000);
    private static final Long DEFAULT_MEMBER_ID = 1L;

    private SettlementPayoutFixture() {
    }

    public static Seller newSeller(Long memberId) {

        return Seller.create(memberId, "store-" + memberId, "biz-" + memberId, "010-0000-0000");
    }

    public static SettlementBatch newBatch(String periodYm) {

        return SettlementBatch.create(periodYm);
    }

    public static SellerSettlement newSettlement(SettlementBatch batch,
                                                 Long sellerId,
                                                 String periodYm,
                                                 BigDecimal rentalAmount,
                                                 BigDecimal feeAmount,
                                                 SellerSettlementStatus status) {

        SellerSettlement settlement = SellerSettlement.create(batch, sellerId, periodYm);
        BigDecimal resolvedRental = rentalAmount != null ? rentalAmount : BigDecimal.ZERO;
        BigDecimal resolvedFee = feeAmount != null ? feeAmount : BigDecimal.ZERO;
        SellerSettlementLine line = SellerSettlementLine.of(
                sellerId,
                RENTAL_ITEM_SEQ.getAndIncrement(),
                DEFAULT_MEMBER_ID,
                PRODUCT_SEQ.getAndIncrement(),
                resolvedRental,
                resolvedFee
        );
        settlement.addLineWithAggregation(line);

        if (status == SellerSettlementStatus.PAID) {
            settlement.pay(LocalDateTime.now());
        } else if (status == SellerSettlementStatus.CANCELED) {
            settlement.cancel();
        }

        return settlement;
    }
}
