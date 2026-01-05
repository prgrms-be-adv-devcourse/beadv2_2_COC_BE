package com.coc.modi.seller.settlement.domain;

import com.coc.modi.common.BaseEntity;
import com.coc.modi.seller.settlement.exception.SellerSettlementStatusConflictException;
import com.coc.modi.seller.settlement.exception.SettlementInputInvalidException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;

@Getter
@Entity
@Table(name = "seller_settlement")
@ToString(of = {"id", "sellerId", "periodYm", "settlementAmount", "status"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerSettlement extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
	@JoinColumn(name = "batch_id")
	private SettlementBatch batch;
	
	@Column(name = "seller_id", nullable = false)
	private Long sellerId;
	
	@Column(name = "period_ym", length = 7)
	private String periodYm;
	
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
	
	public Long getBatchId() {
		
		return batch != null ? batch.getId() : null;
	}
	
	@Builder
	private SellerSettlement(SettlementBatch batch,
							 Long sellerId,
							 String periodYm,
							 BigDecimal totalRentalAmount,
							 BigDecimal totalFeeAmount,
							 BigDecimal settlementAmount,
							 SellerSettlementStatus status,
							 LocalDateTime paidAt) {
		
		this.batch = batch;
		this.sellerId = sellerId;
		this.periodYm = periodYm;
		this.totalRentalAmount = totalRentalAmount != null ? totalRentalAmount : ZERO;
		this.totalFeeAmount = totalFeeAmount != null ? totalFeeAmount : ZERO;
		this.settlementAmount = settlementAmount != null ? settlementAmount : ZERO;
		this.status = status != null ? status : SellerSettlementStatus.READY;
		this.paidAt = paidAt;
	}
	
	public static SellerSettlement create(SettlementBatch batch,
										  Long sellerId,
										  String periodYm) {
		
		return SellerSettlement.builder()
				.batch(batch)
				.sellerId(sellerId)
				.periodYm(periodYm)
				.totalRentalAmount(ZERO)
				.totalFeeAmount(ZERO)
				.settlementAmount(ZERO)
				.status(SellerSettlementStatus.READY)
				.build();
	}
	
	// 정산 완료 처리
	
	public void pay(LocalDateTime paidAt) {
		
		if (paidAt == null) {
			throw new SettlementInputInvalidException("paidAt is required to mark settlement as paid");
		}
		if (this.status == SellerSettlementStatus.PAID) {
			throw new SellerSettlementStatusConflictException("settlement is already paid");
		}
		if (this.status == SellerSettlementStatus.CANCELED) {
			throw new SellerSettlementStatusConflictException("canceled settlement cannot be paid");
		}
		this.status = SellerSettlementStatus.PAID;
		this.paidAt = paidAt;
	}
	
	// 정산 취소
	
	public void cancel() {
		
		if (this.status == SellerSettlementStatus.CANCELED) {
			return;
		}
		if (this.status == SellerSettlementStatus.READY || this.status == SellerSettlementStatus.PAID) {
			this.status = SellerSettlementStatus.CANCELED;
			return;
		}
		throw new SellerSettlementStatusConflictException("unsupported settlement status: " + this.status);
	}
	
	public void addLineWithAggregation(SellerSettlementLine line) {
		// 멱등성: 동일 rentalItemId 중복 방지
		boolean exists = this.lines.stream()
				.anyMatch(existing -> existing.getRentalItemId().equals(line.getRentalItemId()));
		if (exists) {
			return;
		}
		
		line.assignSellerSettlement(this);
		this.lines.add(line);
		this.totalRentalAmount = this.totalRentalAmount.add(line.getRentalAmount());
		this.totalFeeAmount = this.totalFeeAmount.add(line.getFeeAmount());
		this.settlementAmount = this.totalRentalAmount.subtract(this.totalFeeAmount);
	}
	
}
