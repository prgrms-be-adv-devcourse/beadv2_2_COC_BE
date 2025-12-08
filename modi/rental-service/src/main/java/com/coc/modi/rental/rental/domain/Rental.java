package com.coc.modi.rental.rental.domain;

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

        this.paidAt = paidAt;
        updateStatusFromItems();
    }

    public void markCanceled() {

        this.status = RentalStatus.CANCELED;
    }

    public void markReturned() {

        this.status = RentalStatus.COMPLETED;
    }

    public void cancelByMemberRequest() {

        if (this.status == RentalStatus.CANCELED || this.status == RentalStatus.COMPLETED) {

            throw new IllegalStateException("이미 취소되었거나 완료된 대여입니다. rentalStatus: " + this.status);
        }

        if (items != null) {

            boolean hasInProgressItem = items.stream().anyMatch(item -> !item.canCancelByRental());

            if (hasInProgressItem) {

                throw new IllegalStateException("이미 결제되었거나 진행 중인 상품이 있어 취소할 수 없습니다. rentalId: " + this.id);
            }

            items.forEach(RentalItem::cancelByRentalRequest);
        }

        markCanceled();
    }

    public void updateTotalAmount(BigDecimal totalAmount) {

        this.totalAmount = totalAmount;
    }

    public RentalStatus calculateStatus() {

        if (this.status == RentalStatus.CANCELED) {

            return RentalStatus.CANCELED;
        }

        if (items == null || items.isEmpty()) {

            return RentalStatus.REQUESTED;
        }

        long totalCount = items.size();
        long requestedCount = items.stream().filter(item -> item.getStatus() == RentalItemStatus.REQUESTED).count();
        long acceptedCount = items.stream().filter(item -> item.getStatus() == RentalItemStatus.ACCEPTED).count();
        long rentingCount = items.stream().filter(item -> item.getStatus() == RentalItemStatus.RENTING).count();
        long returnedCount = items.stream().filter(item -> item.getStatus() == RentalItemStatus.RETURNED).count();
        long canceledCount = items.stream().filter(item -> item.getStatus() == RentalItemStatus.CANCELED).count();
        long rejectedCount = items.stream().filter(item -> item.getStatus() == RentalItemStatus.REJECTED).count();
        long finishedCount = returnedCount + canceledCount + rejectedCount;

        if (returnedCount == totalCount) {

            return RentalStatus.COMPLETED;
        }

        if (finishedCount == totalCount && returnedCount > 0) {

            return RentalStatus.COMPLETED;
        }

        if (rentingCount > 0) {

            return RentalStatus.IN_PROGRESS;
        }

        if (canceledCount + rejectedCount == totalCount) {

            return RentalStatus.CANCELED;
        }

        if (this.paidAt != null) {

            return RentalStatus.PAID;
        }

        if (acceptedCount == totalCount && totalCount > 0) {

            return RentalStatus.ACCEPTED;
        }

        if (acceptedCount > 0 && acceptedCount < totalCount) {

            return RentalStatus.PARTIALLY_ACCEPTED;
        }

        if (requestedCount == totalCount) {

            return RentalStatus.REQUESTED;
        }

        return RentalStatus.PARTIALLY_ACCEPTED;
    }

    public void updateStatusFromItems() {

        this.status = calculateStatus();
    }

    public void updatePaidAt(LocalDateTime paidAt) {

        markPaid(paidAt);
    }

}
