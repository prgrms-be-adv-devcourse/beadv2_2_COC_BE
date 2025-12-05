package com.coc.modi.rental.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "rental", schema = "public")
public class Rental extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalItem> items = new ArrayList<>();

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RentalStatus status;

    @Column(name = "total_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Builder
    private Rental(Long memberId, RentalStatus status, BigDecimal totalAmount, LocalDateTime paidAt) {

        this.memberId = memberId;
        this.status = status != null ? status : RentalStatus.REQUESTED;
        this.totalAmount = totalAmount;
        this.paidAt = paidAt;
    }

    public static Rental create(Long memberId, BigDecimal totalAmount) {

        return Rental.builder()
                .memberId(memberId)
                .status(RentalStatus.REQUESTED)
                .totalAmount(totalAmount)
                .build();
    }

    public void addItem(RentalItem rentalItem) {

        rentalItem.assignRental(this);
        this.items.add(rentalItem);
    }

    public void markPaid(LocalDateTime paidAt) {

        this.status = RentalStatus.PAID;
        this.paidAt = paidAt;
    }

    public void markCanceled() {

        this.status = RentalStatus.CANCELED;
    }

    public void markReturned() {

        this.status = RentalStatus.COMPLETED;
    }

    public void updateTotalAmount(BigDecimal totalAmount) {

        this.totalAmount = totalAmount;
    }

    public RentalStatus calculateStatus() {

        List<RentalItemStatus> statuses = items.stream()
                .map(RentalItem :: getStatus)
                .toList();

        boolean allCanceledOrRejectedOrReturned = statuses.stream()
                .allMatch(status -> status == RentalItemStatus.CANCELED
                || status == RentalItemStatus.REJECTED
                || status == RentalItemStatus.RETURNED);

        boolean anyRentingOrAcceptedOrRequested = statuses.stream()
                .anyMatch(status -> status == RentalItemStatus.RENTING
                || status == RentalItemStatus.ACCEPTED
                || status == RentalItemStatus.REQUESTED);

        if (allCanceledOrRejectedOrReturned) {

            return RentalStatus.COMPLETED;
        }

        if (anyRentingOrAcceptedOrRequested) {

            return RentalStatus.IN_PROGRESS;
        }

        return RentalStatus.REQUESTED;
    }

}
