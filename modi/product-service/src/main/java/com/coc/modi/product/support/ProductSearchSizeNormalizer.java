package com.coc.modi.product.support;

public final class ProductSearchSizeNormalizer {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	private ProductSearchSizeNormalizer() {
	}

	public static int normalize(int size) {
		if (size <= 0 || size > MAX_PAGE_SIZE) {
			return DEFAULT_PAGE_SIZE;
		}
		return size;
	}
}
