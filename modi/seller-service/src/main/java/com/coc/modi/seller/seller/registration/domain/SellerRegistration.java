package com.coc.modi.seller.seller.registration.domain;

import com.coc.modi.common.BaseEntity;
import com.coc.modi.seller.seller.exception.SellerStatusConflictException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "seller_registration", schema = "seller")
public class SellerRegistration extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", nullable = false, unique = true)
	private Long memberId;

	@Column(name = "store_name", nullable = false, length = 255)
	private String storeName;

	@Column(name = "biz_reg_no", length = 50)
	private String bizRegNo;

	@Column(name = "store_phone", length = 20)
	private String storePhone;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private SellerRegistrationStatus status;

	@Column(name = "approved_by")
	private Long approvedBy;

	@Builder
	private SellerRegistration(Long memberId,
							   String storeName,
							   String bizRegNo,
							   String storePhone,
							   SellerRegistrationStatus status,
							   Long approvedBy) {
		this.memberId = memberId;
		this.storeName = storeName;
		this.bizRegNo = bizRegNo;
		this.storePhone = storePhone;
		this.status = status != null ? status : SellerRegistrationStatus.PENDING;
		this.approvedBy = approvedBy;
	}

	public static SellerRegistration create(Long memberId,
											String storeName,
											String bizRegNo,
											String storePhone) {
		return SellerRegistration.builder()
				.memberId(memberId)
				.storeName(storeName)
				.bizRegNo(bizRegNo)
				.storePhone(storePhone)
				.status(SellerRegistrationStatus.PENDING)
				.approvedBy(null)
				.build();
	}

	public void resubmit(String storeName,
						 String bizRegNo,
						 String storePhone) {
		this.storeName = storeName;
		this.bizRegNo = bizRegNo;
		this.storePhone = storePhone;
		this.status = SellerRegistrationStatus.PENDING;
		this.approvedBy = null;
	}

	public void approve(Long approvedBy) {
		if (this.status == SellerRegistrationStatus.APPROVED) {
			return;
		}
		if (this.status != SellerRegistrationStatus.PENDING) {
			throw new SellerStatusConflictException("seller registration approval is only allowed from PENDING. status=" + this.status);
		}
		this.status = SellerRegistrationStatus.APPROVED;
		this.approvedBy = approvedBy;
	}

	public void reject() {
		if (this.status == SellerRegistrationStatus.REJECTED) {
			return;
		}
		if (this.status != SellerRegistrationStatus.PENDING) {
			throw new SellerStatusConflictException("seller registration rejection is only allowed from PENDING. status=" + this.status);
		}
		this.status = SellerRegistrationStatus.REJECTED;
		this.approvedBy = null;
	}
}
