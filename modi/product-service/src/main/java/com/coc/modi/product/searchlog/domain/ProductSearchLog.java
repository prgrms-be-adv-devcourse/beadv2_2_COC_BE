package com.coc.modi.product.searchlog.domain;

import com.coc.modi.common.BaseEntity;
import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.search.domain.ProductSortType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "product_search_log", schema = "public")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ProductSearchLog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id")
	private Long memberId;

	@Column(nullable = false, length = 200)
	private String keyword;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private ProductCategory category;

	@Column(name = "min_price", precision = 18, scale = 2)
	private BigDecimal minPrice;

	@Column(name = "max_price", precision = 18, scale = 2)
	private BigDecimal maxPrice;

	@Column(name = "seller_id")
	private Long sellerId;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "sort_type", length = 20)
	private ProductSortType sortType;

	@Column(length = 200)
	private String cursor;

	@Column(nullable = false)
	private Integer size;

	private ProductSearchLog(Long memberId,
						 String keyword,
						 ProductCategory category,
						 BigDecimal minPrice,
						 BigDecimal maxPrice,
						 Long sellerId,
						 LocalDate startDate,
						 LocalDate endDate,
						 ProductSortType sortType,
						 String cursor,
						 Integer size) {

		this.memberId = memberId;
		this.keyword = keyword;
		this.category = category;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
		this.sellerId = sellerId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.sortType = sortType;
		this.cursor = cursor;
		this.size = size;
	}

	public static ProductSearchLog create(Long memberId,
								  String keyword,
								  ProductCategory category,
								  BigDecimal minPrice,
								  BigDecimal maxPrice,
								  Long sellerId,
								  LocalDate startDate,
								  LocalDate endDate,
								  ProductSortType sortType,
								  String cursor,
								  Integer size) {

		return new ProductSearchLog(
				memberId,
				keyword,
				category,
				minPrice,
				maxPrice,
				sellerId,
				startDate,
				endDate,
				sortType,
				cursor,
				size
		);
	}
}
