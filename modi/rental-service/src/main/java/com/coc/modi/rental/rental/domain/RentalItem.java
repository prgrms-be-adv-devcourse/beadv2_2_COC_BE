package com.coc.modi.rental.rental.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Table(name = "rental_item", schema = "public")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @OneToMany(mappedBy = "rentalItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalExtend> extendsHistory;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RentalItemStatus status;

    @Column(name = "unit_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Builder
    private RentalItem(Rental rental,
                       Long productId,
                       Long sellerId,
                       LocalDate startDate,
                       LocalDate endDate,
                       RentalItemStatus status,
                       BigDecimal unitPrice,
                       LocalDateTime returnedAt,
                       LocalDateTime canceledAt) {

        this.rental = rental;
        this.productId = productId;
        this.sellerId = sellerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status != null ? status : RentalItemStatus.REQUESTED;
        this.unitPrice = unitPrice;
        this.returnedAt = returnedAt;
        this.canceledAt = canceledAt;
    }

    public static RentalItem create(Long productId,
                                    Long sellerId,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    BigDecimal unitPrice) {

        return RentalItem.builder()
                .productId(productId)
                .sellerId(sellerId)
                .startDate(startDate)
                .endDate(endDate)
                .status(RentalItemStatus.REQUESTED)
                .unitPrice(unitPrice)
                .build();
    }

    void assignRental(Rental rental) {
        this.rental = rental;
    }

    public void decide(RentalItemStatus targetStatus) {

        if (targetStatus != RentalItemStatus.ACCEPTED && targetStatus != RentalItemStatus.REJECTED) {

            throw new IllegalArgumentException("허용되지 않은 상태 변경입니다. targetStatus: " + targetStatus);
        }

        if (this.status != RentalItemStatus.REQUESTED) {

            throw new IllegalStateException("요청 상태에서만 승인/거절이 가능합니다. rentalItemId: " + this.id);
        }

        this.status = targetStatus;

        if (targetStatus == RentalItemStatus.REJECTED) {

            this.canceledAt = LocalDateTime.now();
        }
    }

    public void markPaid() {

        if (this.status != RentalItemStatus.ACCEPTED) {

            throw new IllegalStateException("승인된 상품만 결제할 수 있습니다. rentalItemId: " + this.id);
        }

        this.status = RentalItemStatus.PAID;
    }

    public void markCanceled() {

        if (this.status == RentalItemStatus.RETURNED) {

            throw new IllegalStateException("이미 반납된 상품은 취소할 수 없습니다. rentalItemId: " + this.id);
        }

        this.status = RentalItemStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void markReturned() {

        if (this.status == RentalItemStatus.CANCELED) {

            throw new IllegalStateException("취소된 상품은 반납 처리할 수 없습니다. rentalItemId: " + this.id);
        }

        this.status = RentalItemStatus.RETURNED;
        this.returnedAt = LocalDateTime.now();
    }
}
