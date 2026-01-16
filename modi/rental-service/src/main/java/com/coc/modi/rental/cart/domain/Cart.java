package com.coc.modi.rental.cart.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name = "cart", schema = "rental")
public class Cart {

    private Long memberId;
    private List<CartItem> items = new ArrayList<>();
    private LocalDateTime updatedAt;

    private Cart(Long memberId) {

        this.memberId = memberId;
        this.updatedAt = LocalDateTime.now();
    }

    public static Cart create(Long memberId) {

        return new Cart(memberId);
    }

    public void addItem(CartItem cartItem) {

        this.items.add(cartItem);
        touch();
    }

    public void replaceItems(List<CartItem> newItems) {

        this.items = newItems;
        touch();
    }

    public void touch() {

        this.updatedAt = LocalDateTime.now();
    }
}
