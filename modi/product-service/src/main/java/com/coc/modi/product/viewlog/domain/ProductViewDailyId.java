package com.coc.modi.product.viewlog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ProductViewDailyId implements Serializable {

	@Column(name = "view_date", nullable = false)
	private LocalDate viewDate;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	private ProductViewDailyId(LocalDate viewDate, Long productId) {
		this.viewDate = viewDate;
		this.productId = productId;
	}

	public static ProductViewDailyId of(LocalDate viewDate, Long productId) {
		return new ProductViewDailyId(viewDate, productId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProductViewDailyId that = (ProductViewDailyId) o;
		return Objects.equals(viewDate, that.viewDate)
				&& Objects.equals(productId, that.productId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(viewDate, productId);
	}
}
