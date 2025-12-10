package com.coc.modi.product.domain;

public enum ProductCategory {
    LAPTOP,
    DESKTOP,
    CAMERA,
    TABLET,
    MOBILE,
    MONITOR,
    ACCESSORY,
    DRONE,
    AUDIO,
    PROJECTOR;

    public static ProductCategory from(String value) {
        try {
            return ProductCategory.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("존재하지 않는 카테고리: " + value);
        }
    }
}
