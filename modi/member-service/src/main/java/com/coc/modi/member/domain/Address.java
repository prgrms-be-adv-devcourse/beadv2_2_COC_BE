package com.coc.modi.member.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "address", schema = "public")
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "address_label", length = 50, nullable = false)
    private String addressLabel;

    @Column(name = "recipient_name", length = 100, nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20, nullable = false)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AddressType type;

    @Column(name = "postcode", length = 10, nullable = false)
    private String postcode;

    @Column(name = "road_address", length = 255, nullable = false)
    private String roadAddress;

    @Column(name = "jibun_address", length = 255)
    private String jibunAddress;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

}
