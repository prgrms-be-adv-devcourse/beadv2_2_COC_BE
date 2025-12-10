package com.coc.modi.rental.rental.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
	private List<RentalExtend> extendsHistory = new ArrayList<>();
	
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
					   LocalDateTime canceledAt,
					   List<RentalExtend> extendsHistory) {
		
		this.rental = rental;
		this.productId = productId;
		this.sellerId = sellerId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status != null ? status : RentalItemStatus.REQUESTED;
		this.unitPrice = unitPrice;
		this.returnedAt = returnedAt;
		this.canceledAt = canceledAt;
		if (extendsHistory != null) {
			
			this.extendsHistory = extendsHistory;
		}
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
	
	public void markCanceled() {
		
		if (this.status == RentalItemStatus.RETURNED || this.status == RentalItemStatus.RENTING) {
			
			throw new IllegalStateException(
					"이미 진행된 상품은 취소할 수 없습니다. rentalItemId: " + this.id + ", status: " + this.status);
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
	
	public void cancelByRentalRequest() {
		
		if (!canCancelByRental()) {
			
			throw new IllegalStateException(
					"진행 중이거나 완료된 상품은 취소할 수 없습니다. rentalItemId: " + this.id + ", status: " + this.status);
		}
		
		markCanceled();
	}
	
	public void processReturn() {
		
		if (this.status != RentalItemStatus.RENTING && this.status != RentalItemStatus.ACCEPTED) {
			
			throw new IllegalStateException(
					"현재 상태에서 반납 처리가 불가능합니다. rentalItemId: " + this.id + ", status: " + this.status);
		}
		
		markReturned();
	}
	
	public BigDecimal processRefund() {
		
		if (this.status != RentalItemStatus.ACCEPTED && this.status != RentalItemStatus.RETURNED) {
			
			throw new IllegalStateException(
					"현재 상태에서 환불 처리가 불가능합니다. rentalItemId: " + this.id + ", status: " + this.status);
		}
		
		if (this.status == RentalItemStatus.RETURNED && this.canceledAt != null) {
			
			throw new IllegalStateException("이미 환불 처리된 상품입니다. rentalItemId: " + this.id);
		}
		
		BigDecimal refundAmount = calculateRentalAmount();
		
		if (this.status == RentalItemStatus.ACCEPTED) {
			
			markCanceled();
		} else {
			
			markRefundedAfterReturn();
		}
		
		return refundAmount;
	}
	
	public void markRefundedAfterReturn() {
		
		if (this.status != RentalItemStatus.RETURNED) {
			
			throw new IllegalStateException("반납된 상품만 환불 완료 처리할 수 있습니다. rentalItemId: " + this.id);
		}
		
		if (this.canceledAt != null) {
			
			throw new IllegalStateException("이미 환불된 상품입니다. rentalItemId: " + this.id);
		}
		
		this.canceledAt = LocalDateTime.now();
	}
	
	public BigDecimal extendRental(LocalDate newEndDate) {
		
		if (newEndDate == null) {
			
			throw new IllegalArgumentException("연장 종료일이 필요합니다. rentalItemId: " + this.id);
		}
		
		if (!newEndDate.isAfter(this.endDate)) {
			
			throw new IllegalArgumentException("연장 종료일은 기존 종료일 이후여야 합니다. rentalItemId: " + this.id);
		}
		
		if (this.status == RentalItemStatus.CANCELED
				|| this.status == RentalItemStatus.RETURNED
				|| this.status == RentalItemStatus.REJECTED) {
			
			throw new IllegalStateException(
					"취소/반납/거절된 상품은 연장할 수 없습니다. rentalItemId: " + this.id + ", status: " + this.status);
		}
		
		int extraDays = (int)ChronoUnit.DAYS.between(this.endDate, newEndDate);
		
		if (extraDays <= 0) {
			
			throw new IllegalArgumentException("연장 일수는 1일 이상이어야 합니다. rentalItemId: " + this.id);
		}
		
		BigDecimal extraAmount = this.unitPrice
				.multiply(BigDecimal.valueOf(extraDays))
				.setScale(2, RoundingMode.HALF_UP);
		
		RentalExtend extendHistory = RentalExtend.create(this, this.endDate, newEndDate, extraDays, extraAmount);
		
		this.endDate = newEndDate;
		this.extendsHistory.add(extendHistory);
		
		return extraAmount;
	}
	
	public BigDecimal calculateRentalAmount() {
		
		long rentalDays = ChronoUnit.DAYS.between(this.startDate, this.endDate) + 1;
		
		if (rentalDays <= 0) {
			
			throw new IllegalStateException("대여 종료일이 시작일보다 빠릅니다. rentalItemId: " + this.id);
		}
		
		return this.unitPrice
				.multiply(BigDecimal.valueOf(rentalDays))
				.setScale(2, RoundingMode.HALF_UP);
	}
	
	public boolean isFinished() {
		
		return this.status == RentalItemStatus.RETURNED
				|| this.status == RentalItemStatus.CANCELED
				|| this.status == RentalItemStatus.REJECTED;
	}
	
	public boolean canCancelByRental() {
		
		return this.status != RentalItemStatus.RENTING
				&& this.status != RentalItemStatus.RETURNED;
	}
	
	public void cancelByMemberRequest() {
		
		if (this.status == RentalItemStatus.CANCELED) {
			
			throw new IllegalStateException("이미 취소된 대여입니다. rentalStatus: " + this.status);
		}
		
		markCanceled();
	}
}
