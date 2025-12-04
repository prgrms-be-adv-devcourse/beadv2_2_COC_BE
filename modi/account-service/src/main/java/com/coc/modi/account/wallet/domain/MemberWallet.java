<<<<<<<< HEAD:modi/account-service/src/main/java/com/coc/modi/account/member/domain/MemberWallet.java
package com.coc.modi.account.member.domain;
========
package com.coc.modi.account.wallet.domain;
>>>>>>>> dev:modi/account-service/src/main/java/com/coc/modi/account/wallet/domain/MemberWallet.java

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "member_wallet", schema = "public")
public class MemberWallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;
<<<<<<<< HEAD:modi/account-service/src/main/java/com/coc/modi/account/member/domain/MemberWallet.java
========

    @Version
    private Long version;
>>>>>>>> dev:modi/account-service/src/main/java/com/coc/modi/account/wallet/domain/MemberWallet.java
}
