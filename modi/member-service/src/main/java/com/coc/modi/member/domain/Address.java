package com.coc.modi.member.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "address", schema = "public")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "address_label", length = 50)
    private String addressLabel;

    @Column(name = "recipient_name", nullable = false, length = 50)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AddressType type;

    @Column(name = "postcode", nullable = false, length = 10)
    private String postcode;

    @Column(name = "road_address", nullable = false, length = 255)
    private String roadAddress;

    @Column(name = "detail_address", nullable = false, length = 255)
    private String detailAddress;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    private Address(Member member,
                    String addressLabel,
                    String recipientName,
                    String recipientPhone,
                    AddressType type,
                    String postcode,
                    String roadAddress,
                    String detailAddress,
                    boolean isDefault) {

        this.member = member;
        this.addressLabel = addressLabel;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.type = type;
        this.postcode = postcode;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
    }

    public static Address create(Member member,
                                 String addressLabel,
                                 String recipientName,
                                 String recipientPhone,
                                 AddressType type,
                                 String postcode,
                                 String roadAddress,
                                 String detailAddress,
                                 boolean isDefault) {

        return new Address(member, addressLabel, recipientName, recipientPhone, type, postcode, roadAddress, detailAddress, isDefault);
    }

    public void updateDefault(boolean isDefault) {

        this.isDefault = isDefault;
    }

    public void update(String addressLabel,
                       String recipientName,
                       String recipientPhone,
                       AddressType type,
                       String postcode,
                       String roadAddress,
                       String detailAddress) {

        this.addressLabel = addressLabel;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.type = type;
        this.postcode = postcode;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
    }
}
