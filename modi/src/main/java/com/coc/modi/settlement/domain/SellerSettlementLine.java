package com.coc.modi.settlement.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "seller_settlement_line")
public class SellerSettlementLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_settlement_id", nullable = false)
    private SellerSettlement sellerSettlement;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "rental_id", nullable = false)
    private Long rentalId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "rental_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal rentalAmount;

    @Column(name = "fee_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal feeAmount;

    @Builder
    private SellerSettlementLine(Long sellerId,
                                 Long rentalId,
                                 Long memberId,
                                 Long productId,
                                 BigDecimal rentalAmount,
                                 BigDecimal feeAmount) {
        this.sellerId = sellerId;
        this.rentalId = rentalId;
        this.memberId = memberId;
        this.productId = productId;
        this.rentalAmount = rentalAmount;
        this.feeAmount = feeAmount;
    }

    public static SellerSettlementLine of(Long sellerId,
                                          Long rentalId,
                                          Long memberId,
                                          Long productId,
                                          BigDecimal rentalAmount,
                                          BigDecimal feeAmount) {
        return SellerSettlementLine.builder()
                .sellerId(sellerId)
                .rentalId(rentalId)
                .memberId(memberId)
                .productId(productId)
                .rentalAmount(rentalAmount)
                .feeAmount(feeAmount)
                .build();
    }

    void assignSellerSettlement(SellerSettlement sellerSettlement) {
        this.sellerSettlement = sellerSettlement;
    }
}
