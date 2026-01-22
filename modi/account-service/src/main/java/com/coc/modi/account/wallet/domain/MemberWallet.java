package com.coc.modi.account.wallet.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "member_wallet",
        schema = "account",
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

    @Column(name = "non_card_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal nonCardBalance = BigDecimal.ZERO;

    @Version
    @Column(nullable = false)
    private Long version;

    public static MemberWallet create(Long memberId) {

        MemberWallet wallet = new MemberWallet();

        wallet.memberId = memberId;
        wallet.balance = BigDecimal.ZERO;
        wallet.nonCardBalance = BigDecimal.ZERO;
        wallet.version = 0L;

        return wallet;
    }

    public void changeBalance(BigDecimal balanceAfter) {

        this.balance = balanceAfter;
    }

    public void changeNonCardBalance(BigDecimal balanceAfter) {

        this.nonCardBalance = balanceAfter;
    }
}
