package com.coc.modi.product.product.domain;

import com.coc.modi.product.product.exception.ProductInvalidInputException;

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
			throw new ProductInvalidInputException("존재하지 않는 카테고리입니다: " + value);
		}
	}
}
