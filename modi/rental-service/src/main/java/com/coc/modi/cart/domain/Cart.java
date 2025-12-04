<<<<<<<< HEAD:modi/rental-service/src/main/java/com/coc/modi/rental/domain/Cart.java
package com.coc.modi.rental.domain;
========
package com.coc.modi.cart.domain;
>>>>>>>> dev:modi/rental-service/src/main/java/com/coc/modi/cart/domain/Cart.java

import com.coc.modi.account.member.domain.Member;
import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "cart", schema = "public")
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

}
