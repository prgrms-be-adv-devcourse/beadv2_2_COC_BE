package com.coc.modi.rental.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                       LocalDate startDate,
                       LocalDate endDate,
                       RentalItemStatus status,
                       BigDecimal unitPrice,
                       LocalDateTime returnedAt,
                       LocalDateTime canceledAt) {
        this.rental = rental;
        this.productId = productId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status != null ? status : RentalItemStatus.REQUESTED;
        this.unitPrice = unitPrice;
        this.returnedAt = returnedAt;
        this.canceledAt = canceledAt;
    }

    public static RentalItem create(Long productId,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    BigDecimal unitPrice) {
        return RentalItem.builder()
                .productId(productId)
                .startDate(startDate)
                .endDate(endDate)
                .status(RentalItemStatus.REQUESTED)
                .unitPrice(unitPrice)
                .build();
    }

    void assignRental(Rental rental) {
        this.rental = rental;
    }
}
