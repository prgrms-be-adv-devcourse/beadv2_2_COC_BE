package com.coc.modi.rental.cart.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name = "cart_item", schema = "rental")
public class CartItem {

    private Long id;
    private Long productId;
    private LocalDate startDate;
    private LocalDate endDate;

    private CartItem(Long id, Long productId, LocalDate startDate, LocalDate endDate) {

        this.id = id;
        this.productId = productId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static CartItem create(Long id, Long productId, LocalDate startDate, LocalDate endDate) {

        return new CartItem(id, productId, startDate, endDate);
    }

    public CartItem updatePeriod(LocalDate startDate, LocalDate endDate) {

        this.startDate = startDate;
        this.endDate = endDate;

        return this;
    }
}
