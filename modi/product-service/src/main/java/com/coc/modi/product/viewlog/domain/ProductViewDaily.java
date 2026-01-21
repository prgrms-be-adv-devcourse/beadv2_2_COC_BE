package com.coc.modi.product.viewlog.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "product_view_daily")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ProductViewDaily extends BaseEntity {

	@EmbeddedId
	private ProductViewDailyId id;

	@Column(name = "view_count", nullable = false)
	private Long viewCount;

	private ProductViewDaily(ProductViewDailyId id, Long viewCount) {
		this.id = id;
		this.viewCount = viewCount;
	}

	public static ProductViewDaily create(LocalDate viewDate, Long productId, Long viewCount) {
		return new ProductViewDaily(ProductViewDailyId.of(viewDate, productId), viewCount);
	}

	public void increment(long delta) {
		this.viewCount = this.viewCount + delta;
	}
}
