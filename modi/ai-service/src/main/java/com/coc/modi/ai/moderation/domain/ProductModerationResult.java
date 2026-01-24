package com.coc.modi.ai.moderation.domain;

import java.util.UUID;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product_moderation_result", schema = "ai")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductModerationResult extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Enumerated(EnumType.STRING)
	@Column(name = "decision", nullable = false, length = 20)
	private ProductModerationDecision decision;

	@Column(name = "score")
	private Double score;

	@Column(name = "reasons", columnDefinition = "text")
	private String reasons;

	@Column(name = "message", columnDefinition = "text")
	private String message;

	@Column(name = "request_event_id", length = 100)
	private String requestEventId;

	@Column(name = "source", length = 50)
	private String source;

	private ProductModerationResult(Long productId,
									ProductModerationDecision decision,
									Double score,
									String reasons,
									String message,
									String requestEventId,
									String source) {
		this.productId = productId;
		this.decision = decision;
		this.score = score;
		this.reasons = reasons;
		this.message = message;
		this.requestEventId = requestEventId;
		this.source = source;
	}

	public static ProductModerationResult create(Long productId,
												 ProductModerationDecision decision,
												 Double score,
												 String reasons,
												 String message,
												 String requestEventId,
												 String source) {

		return new ProductModerationResult(
				productId,
				decision,
				score,
				reasons,
				message,
				requestEventId,
				source
		);
	}
}
