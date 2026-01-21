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
@Table(name = "rental", schema = "rental")
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
	
	public void updateTotalAmount(BigDecimal totalAmount) {
		
		this.totalAmount = totalAmount;
	}
	
	public void recalculateAmountsAndStatus() {
		
		recalculateTotalAmount();
		updateStatusFromItems();
	}
	
	public BigDecimal recalculateTotalAmount() {
		
		if (items == null || items.isEmpty()) {
			
			this.totalAmount = BigDecimal.ZERO;
			return this.totalAmount;
		}
		
		BigDecimal recalculated = items.stream()
				.filter(this::isChargeableItem)
				.map(RentalItem::calculateRentalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		this.totalAmount = recalculated;
		return this.totalAmount;
	}
	
	private boolean isChargeableItem(RentalItem rentalItem) {
		
		if (rentalItem == null) {
			
			return false;
		}
		
		if (rentalItem.getCanceledAt() != null) {
			
			return false;
		}
		
		RentalItemStatus status = rentalItem.getStatus();
		
		return status != RentalItemStatus.CANCELED && status != RentalItemStatus.REJECTED;
	}
	
	public RentalStatus calculateStatus() {
		
		if (items == null || items.isEmpty()) {
			
			return RentalStatus.REQUESTED;
		}
		
		long canceledOrRejectedCount = items.stream()
				.filter(item -> item.getStatus() == RentalItemStatus.CANCELED || item.getStatus() == RentalItemStatus.REJECTED)
				.count();
		
		if (canceledOrRejectedCount == items.size()) {
			
			return RentalStatus.CANCELED;
		}
		
		List<RentalItem> chargeableItems = items.stream()
				.filter(this::isChargeableItem)
				.toList();
		
		if (chargeableItems.isEmpty()) {
			
			return RentalStatus.CANCELED;
		}
		
		long totalChargeable = chargeableItems.size();
		long requestedCount = chargeableItems.stream().filter(item -> item.getStatus() == RentalItemStatus.REQUESTED).count();
		long acceptedCount = chargeableItems.stream().filter(item -> item.getStatus() == RentalItemStatus.ACCEPTED).count();
		long rentingCount = chargeableItems.stream().filter(item -> item.getStatus() == RentalItemStatus.RENTING).count();
		long returnedCount = chargeableItems.stream().filter(item -> item.getStatus() == RentalItemStatus.RETURNED).count();
		long canceledCount = chargeableItems.stream().filter(item -> item.getStatus() == RentalItemStatus.CANCELED).count();
		long rejectedCount = chargeableItems.stream().filter(item -> item.getStatus() == RentalItemStatus.REJECTED).count();
		long finishedCount = returnedCount + canceledCount + rejectedCount;
		
		if (returnedCount == totalChargeable) {
			
			return RentalStatus.COMPLETED;
		}
		
		if (finishedCount == totalChargeable && returnedCount > 0) {
			
			return RentalStatus.COMPLETED;
		}
		
		if (rentingCount > 0) {
			
			return RentalStatus.IN_PROGRESS;
		}
		
		if (this.paidAt != null) {
			
			return RentalStatus.PAID;
		}
		
		if (acceptedCount == totalChargeable && totalChargeable > 0) {
			
			return RentalStatus.ACCEPTED;
		}
		
		if (acceptedCount > 0 && acceptedCount < totalChargeable) {
			
			return RentalStatus.PARTIALLY_ACCEPTED;
		}
		
		if (requestedCount == totalChargeable) {
			
			return RentalStatus.REQUESTED;
		}
		
		return RentalStatus.PARTIALLY_ACCEPTED;
	}
	
	public void updateStatusFromItems() {
		
		this.status = calculateStatus();
	}
	
}
