package com.coc.modi.settlement.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "seller_settlement")
public class SellerSettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "total_rental_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalRentalAmount;

    @Column(name = "total_fee_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalFeeAmount;

    @Column(name = "settlement_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal settlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SellerSettlementStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "sellerSettlement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SellerSettlementLine> lines = new ArrayList<>();

    @Builder
    private SellerSettlement(
                             Long sellerId,
                             BigDecimal totalRentalAmount,
                             BigDecimal totalFeeAmount,
                             BigDecimal settlementAmount,
                             SellerSettlementStatus status,
                             LocalDateTime paidAt) {
        this.sellerId = sellerId;
        this.totalRentalAmount = totalRentalAmount;
        this.totalFeeAmount = totalFeeAmount;
        this.settlementAmount = settlementAmount;
        this.status = status != null ? status : SellerSettlementStatus.READY;
        this.paidAt = paidAt;
    }

    public static SellerSettlement create(
                                          Long sellerId,
                                          BigDecimal totalRentalAmount,
                                          BigDecimal totalFeeAmount,
                                          BigDecimal settlementAmount) {
        return SellerSettlement.builder()
                .sellerId(sellerId)
                .totalRentalAmount(totalRentalAmount)
                .totalFeeAmount(totalFeeAmount)
                .settlementAmount(settlementAmount)
                .status(SellerSettlementStatus.READY)
                .build();
    }

    public void addLine(SellerSettlementLine line) {
        line.assignSellerSettlement(this);
        this.lines.add(line);
    }

    public void markPaid(LocalDateTime paidAt) {
        this.status = SellerSettlementStatus.PAID;
        this.paidAt = paidAt;
    }

    public void cancel() {
        this.status = SellerSettlementStatus.CANCELED;
    }
}
