package com.coc.modi.product.viewlog.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "product_view_log", schema = "public")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ProductViewLog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "member_id")
	private Long memberId;

	@Column(name = "view_date", nullable = false)
	private LocalDate viewDate;

	private ProductViewLog(Long productId, Long memberId, LocalDate viewDate) {
		this.productId = productId;
		this.memberId = memberId;
		this.viewDate = viewDate;
	}

	public static ProductViewLog create(Long productId, Long memberId, LocalDate viewDate) {
		return new ProductViewLog(productId, memberId, viewDate);
	}
}
