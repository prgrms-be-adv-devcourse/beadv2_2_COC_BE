package com.coc.modi.product.product.search.domain;

public enum ProductSortType {
	LATEST,       // createdAt DESC
	OLDEST,       // createdAt ASC
	PRICE_HIGH,   // pricePerDay DESC
	PRICE_LOW     // pricePerDay ASC
}
