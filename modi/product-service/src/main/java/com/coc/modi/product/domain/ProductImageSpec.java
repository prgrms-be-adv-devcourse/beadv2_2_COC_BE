package com.coc.modi.product.domain;

public record ProductImageSpec(
        Long id,        // 기존 이미지면 id 포함, 새 이미지면 null
        String url,
        Integer ordering
) {
}
