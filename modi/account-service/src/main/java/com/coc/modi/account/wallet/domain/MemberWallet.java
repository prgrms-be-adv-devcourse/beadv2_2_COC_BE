package com.coc.modi.account.wallet.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "member_wallet",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_wallet_member_id", columnNames = "member_id")
        }
)
public class MemberWallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    @Column(nullable = false)
    private Long version;
}
