package com.coc.modi.product.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
		name = "product_outbox",
		schema = "product",
		indexes = {
				@Index(name = "idx_product_outbox_status_created", columnList = "status, created_at")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOutboxEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "aggregate_type", nullable = false, length = 30)
	private String aggregateType;

	@Column(name = "aggregate_id", nullable = false)
	private Long aggregateId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 40)
	private ProductOutboxEventType eventType;

	@Column(name = "payload", nullable = false, columnDefinition = "text")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ProductOutboxStatus status;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

	@Column(name = "last_error", length = 500)
	private String lastError;

	@Version
	private long version;

	private ProductOutboxEvent(String aggregateType,
							   Long aggregateId,
							   ProductOutboxEventType eventType,
							   String payload) {

		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.payload = payload;
		this.status = ProductOutboxStatus.PENDING;
		this.retryCount = 0;
	}

	public static ProductOutboxEvent create(String aggregateType,
											Long aggregateId,
											ProductOutboxEventType eventType,
											String payload) {

		return new ProductOutboxEvent(aggregateType, aggregateId, eventType, payload);
	}

	public void markSent() {

		this.status = ProductOutboxStatus.SENT;
		this.processedAt = LocalDateTime.now();
		this.lastError = null;
	}

	public void markFailed(String error, int maxRetries) {

		this.retryCount += 1;
		this.lastError = truncate(error, 500);

		if (this.retryCount >= maxRetries) {
			this.status = ProductOutboxStatus.FAILED;
			this.processedAt = LocalDateTime.now();
		}
	}

	private String truncate(String value, int maxLength) {

		if (value == null) {
			return null;
		}
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}
}
