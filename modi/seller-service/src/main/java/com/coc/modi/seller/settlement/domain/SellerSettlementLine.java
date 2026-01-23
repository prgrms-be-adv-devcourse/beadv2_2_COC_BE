package com.coc.modi.seller.settlement.domain;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
		name = "seller_settlement_line",
		schema = "seller",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_seller_settlement_line_settlement_rental_item",
				columnNames = {"seller_settlement_id", "rental_item_id"}
		)
)
public class SellerSettlementLine extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seller_settlement_id", nullable = false)
	private SellerSettlement sellerSettlement;
	
	@Column(name = "seller_id", nullable = false)
	private Long sellerId;
	
	@Column(name = "rental_item_id", nullable = false)
	private Long rentalItemId;
	
	@Column(name = "member_id", nullable = false)
	private Long memberId;
	
	@Column(name = "product_id", nullable = false)
	private Long productId;
	
	@Column(name = "rental_amount", nullable = false, precision = 18, scale = 2)
	private BigDecimal rentalAmount;
	
	@Column(name = "fee_amount", nullable = false, precision = 18, scale = 2)
	private BigDecimal feeAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
	private SellerSettlementLineStatus status;

	@Column(name = "canceled_at")
	private LocalDateTime canceledAt;
	
	@Builder
	private SellerSettlementLine(Long sellerId,
								 Long rentalItemId,
								 Long memberId,
								 Long productId,
								 BigDecimal rentalAmount,
								 BigDecimal feeAmount,
								 SellerSettlementLineStatus status,
								 LocalDateTime canceledAt) {
		
		this.sellerId = sellerId;
		this.rentalItemId = rentalItemId;
		this.memberId = memberId;
		this.productId = productId;
		this.rentalAmount = rentalAmount;
		this.feeAmount = feeAmount;
		this.status = status != null ? status : SellerSettlementLineStatus.ACTIVE;
		this.canceledAt = canceledAt;
	}
	
	public static SellerSettlementLine of(Long sellerId,
										  Long rentalItemId,
										  Long memberId,
										  Long productId,
										  BigDecimal rentalAmount,
										  BigDecimal feeAmount) {
		
		return SellerSettlementLine.builder()
				.sellerId(sellerId)
				.rentalItemId(rentalItemId)
				.memberId(memberId)
				.productId(productId)
				.rentalAmount(rentalAmount)
				.feeAmount(feeAmount)
				.status(SellerSettlementLineStatus.ACTIVE)
				.build();
	}
	
	void assignSellerSettlement(SellerSettlement sellerSettlement) {
		
		this.sellerSettlement = sellerSettlement;
	}
}
