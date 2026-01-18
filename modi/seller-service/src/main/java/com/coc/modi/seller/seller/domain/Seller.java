package com.coc.modi.seller.seller.domain;

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
@Table(name = "seller", schema = "seller")
public class Seller extends BaseEntity {

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
    private SellerStatus status;

    @Builder
    private Seller(Long memberId,
                   String storeName,
                   String bizRegNo,
                   String storePhone,
                   SellerStatus status) {
        this.memberId = memberId;
        this.storeName = storeName;
        this.bizRegNo = bizRegNo;
        this.storePhone = storePhone;
        this.status = status != null ? status : SellerStatus.PENDING;
    }

    public static Seller create(Long memberId,
                                String storeName,
                                String bizRegNo,
                                String storePhone) {
        return Seller.builder()
                .memberId(memberId)
                .storeName(storeName)
                .bizRegNo(bizRegNo)
                .storePhone(storePhone)
                .status(SellerStatus.PENDING)
                .build();
    }

    public void requestApproval() {
        if (this.status == SellerStatus.PENDING) {
            return;
        }
        if (this.status == SellerStatus.ACTIVE || this.status == SellerStatus.SUSPENDED
                || this.status == SellerStatus.CLOSED || this.status == SellerStatus.REJECTED) {
            throw new SellerStatusConflictException("seller approval request is not allowed. status=" + this.status);
        }
        this.status = SellerStatus.PENDING;
    }

    public void approve() {
        if (this.status == SellerStatus.ACTIVE) {
            return;
        }
        if (this.status != SellerStatus.PENDING) {
            throw new SellerStatusConflictException("seller approval is only allowed from PENDING. status=" + this.status);
        }
        this.status = SellerStatus.ACTIVE;
    }

    public void reject() {
        if (this.status == SellerStatus.REJECTED) {
            return;
        }
        if (this.status != SellerStatus.PENDING) {
            throw new SellerStatusConflictException("seller rejection is only allowed from PENDING. status=" + this.status);
        }
        this.status = SellerStatus.REJECTED;
    }

    public void update(String storeName,
                       String bizRegNo,
                       String storePhone) {
        this.storeName = storeName;
        this.bizRegNo = bizRegNo;
        this.storePhone = storePhone;
    }

    public void changeStatus(SellerStatus status) {
        if (status != null) {
            this.status = status;
        }
    }
}
