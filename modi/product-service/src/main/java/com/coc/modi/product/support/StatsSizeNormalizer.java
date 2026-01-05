package com.coc.modi.product.support;

public final class StatsSizeNormalizer {
	
	public static final int DEFAULT_SIZE = 10;
	public static final int MAX_SIZE = 100;
	
	private StatsSizeNormalizer() {
	
	}
	
	public static int normalize(Integer size) {
		
		if (size == null || size <= 0) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}
}
