<<<<<<<< HEAD:modi/rental-service/src/main/java/com/coc/modi/rental/domain/CartItem.java
package com.coc.modi.rental.domain;
========
package com.coc.modi.cart.domain;
>>>>>>>> dev:modi/rental-service/src/main/java/com/coc/modi/cart/domain/CartItem.java

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "cart_item", schema = "public")
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

<<<<<<<< HEAD:modi/rental-service/src/main/java/com/coc/modi/rental/domain/CartItem.java
    @Column(name = "rental_start_date", nullable = false)
    private LocalDate rentalStartDate;

    @Column(name = "rental_end_date", nullable = false)
    private LocalDate rentalEndDate;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;
========
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
>>>>>>>> dev:modi/rental-service/src/main/java/com/coc/modi/cart/domain/CartItem.java
}
