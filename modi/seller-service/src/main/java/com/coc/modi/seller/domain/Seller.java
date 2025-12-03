package com.coc.modi.seller.domain;

import com.coc.modi.common.BaseEntity;
import com.coc.modi.account.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "seller", schema = "public")
public class Seller extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "seller_name", nullable = false, length = 100)
    private String sellerName;

    @Column(name = "business_registration_number", length = 50)
    private String businessRegistrationNumber;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SellerStatus status;

}
